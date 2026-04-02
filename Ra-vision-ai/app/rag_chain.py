import os
from langchain_huggingface import HuggingFaceEndpoint, ChatHuggingFace
from langchain_core.prompts import ChatPromptTemplate, SystemMessagePromptTemplate, HumanMessagePromptTemplate
from langchain_core.output_parsers import StrOutputParser

from .database import (
    buscar_funcionario_por_matricula,
    buscar_total_vendas_funcionario,
    buscar_vendas_loja,
    buscar_comissao_calculada,
    buscar_percentual_comissao,
    buscar_gerentes_loja
)
from .intent_parser import parse_intent
from .prompts import SYSTEM_PROMPT, build_context_prompt

def get_rag_chain():
    """
    Configura a chain do LangChain com HuggingFace.
    """
    # 1. Configurar o LLM (Llama 3.3 70B via HF Inference API)
    llm = HuggingFaceEndpoint(
        repo_id="meta-llama/Llama-3.3-70B-Instruct",
        task="text-generation",
        max_new_tokens=1024,
        do_sample=False,
        temperature=0.1,
        huggingfacehub_api_token=os.getenv("HUGGINGFACEHUB_API_TOKEN")
    )
    
    chat_model = ChatHuggingFace(llm=llm)
    
    # 2. Definir o Prompt Template
    prompt = ChatPromptTemplate.from_messages([
        SystemMessagePromptTemplate.from_template(SYSTEM_PROMPT),
        HumanMessagePromptTemplate.from_template("{context}")
    ])
    
    # 3. Montar a Chain
    chain = prompt | chat_model | StrOutputParser()
    return chain

async def process_user_query(message: str, date_ref: str):
    """
    Pipeline RAG completo com logs detalhados para debug.
    """
    try:
        # 1. Parse de intenção
        intent = parse_intent(message, date_ref)
        # Prioriza a data detectada no texto (ex: "em Outubro") sobre o seletor
        target_date = intent.date_ref or date_ref
        
        print(f"DEBUG: Processando query. Msg: {message}, Data Alvo: {target_date}, Tipo: {intent.tipo_consulta}")
        
        # 2. Retrieval
        context_data = {
            "dados_rh": [],
            "vendas_individuais": 0.0,
            "vendas_loja": 0.0,
            "comissao_calculada": [],
            "percentual_info": None
        }
        
        # Caso A: Busca por Matrícula
        if intent.matricula:
            print(f"DEBUG: Buscando por matrícula {intent.matricula}...")
            context_data["dados_rh"] = buscar_funcionario_por_matricula(intent.matricula, target_date)
        
        # Caso B: Busca por Gerente de Loja (quando não informou matrícula)
        elif intent.cod_loja and intent.tipo_consulta == "gerente":
            print(f"DEBUG: Buscando gerente da loja {intent.cod_loja}...")
            context_data["dados_rh"] = buscar_gerentes_loja(intent.cod_loja, target_date)

        # Enriquecimento de dados se encontrou alguém
        if context_data["dados_rh"]:
            # Se encontrou múltiplos (ex: pediu todos os gerentes), pegamos o primeiro para detalhes individuais se necessário
            # mas o prompt receberá a lista completa no dados_rh
            rh_principal = context_data["dados_rh"][0]
            matricula_alvo = rh_principal.get("matricula")
            cod_loja_alvo = rh_principal.get("cod_loja")
            
            context_data["vendas_individuais"] = buscar_total_vendas_funcionario(matricula_alvo, target_date)
            context_data["vendas_loja"] = buscar_vendas_loja(cod_loja_alvo, target_date)
            context_data["comissao_calculada"] = buscar_comissao_calculada(matricula_alvo, target_date)
            context_data["percentual_info"] = buscar_percentual_comissao(
                rh_principal.get("cod_marca"), 
                rh_principal.get("cod_cargo")
            )
            print(f"DEBUG: Enriquecimento concluído para {matricula_alvo}.")
        else:
            print(f"DEBUG: Nenhum dado de RH encontrado para os critérios.")
                
        # 3. Build Context Prompt
        context_prompt = build_context_prompt(
            dados_rh=context_data["dados_rh"],
            vendas_individuais=context_data["vendas_individuais"],
            vendas_loja=context_data["vendas_loja"],
            comissao_calculada=context_data["comissao_calculada"],
            percentual_info=context_data["percentual_info"],
            pergunta=message
        )
        
        # 4. Generation
        print(f"DEBUG: Chamando LLM Hugging Face via InferenceClient...")
        try:
            from huggingface_hub import InferenceClient
            
            client = InferenceClient(
                model="meta-llama/Llama-3.1-8B-Instruct",
                token=os.getenv("HUGGINGFACEHUB_API_TOKEN")
            )
            
            # Construindo a mensagem para o chat
            messages = [
                {"role": "system", "content": SYSTEM_PROMPT},
                {"role": "user", "content": context_prompt}
            ]
            
            response = ""
            for message_chunk in client.chat_completion(
                messages,
                max_tokens=1024,
                temperature=0.1,
                stream=True,
            ):
                token_text = message_chunk.choices[0].delta.content
                if token_text:
                    response += token_text
            
            print(f"DEBUG: Resposta recebida com sucesso.")
            return response
            
        except Exception as llm_err:
            print(f"ERROR: Falha no InferenceClient: {llm_err}")
            return f"Erro na IA (Inference API): {llm_err}. Verifique se o seu token tem permissões de 'Read' no Hugging Face."

    except Exception as e:
        print(f"CRITICAL ERROR em process_user_query: {e}")
        return f"Erro crítico: {str(e)}"
