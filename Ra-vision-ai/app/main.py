import os
import time
import asyncio
import httpx
from datetime import datetime
import json
from fastapi import FastAPI, HTTPException, Request, BackgroundTasks
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from dotenv import load_dotenv

from .rag_chain import process_user_query
from .database import buscar_competencias_disponiveis

load_dotenv()

app = FastAPI(title="Ra Vision AI Orchestrator")

# Configurar CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"], # Ajustar conforme necessário em produção
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Configurações de Monitoramento
JAVA_BACKEND_URL = os.getenv("JAVA_BACKEND_URL", "http://localhost:8080/api/monitoramento/ia")
API_KEY = os.getenv("RAVISION_AI_API_KEY", "RAVISION_SECURE_API_KEY_2026")

async def enviar_metricas_com_retry(payload: dict):
    """
    Envia as métricas para o backend Java de forma assíncrona, com retry e fallback em arquivo local.
    """
    max_retries = 3
    base_delay = 2 # segundos
    
    headers = {
        "Content-Type": "application/json",
        "X-API-Key": API_KEY
    }
    
    async with httpx.AsyncClient() as client:
        for attempt in range(max_retries):
            try:
                response = await client.post(JAVA_BACKEND_URL, json=payload, headers=headers, timeout=5.0)
                response.raise_for_status()
                return # Sucesso!
            except httpx.HTTPError as exc:
                print(f"Erro ao enviar métricas (Tentativa {attempt + 1}/{max_retries}): {exc}")
                if attempt < max_retries - 1:
                    await asyncio.sleep(base_delay * (2 ** attempt)) # Backoff exponencial
                else:
                    # Fallback
                    print("Todas as tentativas falharam. Gravando métricas localmente em fallback.")
                    try:
                        with open("monitoramento_fallback.log", "a", encoding="utf-8") as f:
                            f.write(json.dumps(payload, ensure_ascii=False) + "\n")
                    except Exception as fallback_exc:
                        print(f"Erro no fallback local: {fallback_exc}")

class ChatRequest(BaseModel):
    message: str
    dateRef: str # Formato yyyy-MM-dd

class ChatResponse(BaseModel):
    response: str

@app.get("/health")
def health_check():
    return {"status": "ok", "service": "ra-vision-ai"}

@app.get("/api/competencias")
def get_competencias():
    try:
        return buscar_competencias_disponiveis()
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post("/api/chat", response_model=ChatResponse)
async def chat_endpoint(request: ChatRequest, req: Request, background_tasks: BackgroundTasks):
    """
    Endpoint principal que recebe a mensagem do usuário e retorna a explicação da IA.
    Também envia métricas de monitoramento em background.
    """
    if not request.message:
        raise HTTPException(status_code=400, detail="Mensagem vazia")
    
    start_time = time.time()
    
    auth_header = req.headers.get("Authorization")
    # Obtendo o usuarioLogado do header (frontend deve enviar, ou fallback)
    usuario_logado = req.headers.get("X-User-ID", "DESCONHECIDO")
    
    # Prepara objeto da métrica com valores defaults caso ocorra erro
    metrica_payload = {
        "timestamp": datetime.now().isoformat(),
        "perguntaUsuario": request.message,
        "respostaIA": "",
        "tempoRespostaMs": 0,
        "ferramentaUtilizada": "RAG_Chain", # Poderia ser extraído do LangChain callbacks
        "sucessoFerramenta": False,
        "usuarioLogado": usuario_logado
    }
    
    try:
        response_text = await process_user_query(request.message, request.dateRef, auth_header)
        
        # Concluiu com sucesso
        metrica_payload["respostaIA"] = response_text
        metrica_payload["sucessoFerramenta"] = True
        
        return ChatResponse(response=response_text)
    except Exception as e:
        erro_msg = f"Erro interno: {str(e)}"
        print(f"Erro no endpoint de chat: {e}")
        metrica_payload["respostaIA"] = erro_msg
        metrica_payload["sucessoFerramenta"] = False
        raise HTTPException(status_code=500, detail="Erro interno ao processar chat")
    finally:
        # Finaliza cronômetro independentemente de sucesso ou erro
        elapsed_ms = int((time.time() - start_time) * 1000)
        metrica_payload["tempoRespostaMs"] = elapsed_ms
        
        # Despacha o envio das métricas para o background sem bloquear a resposta ao usuário
        background_tasks.add_task(enviar_metricas_com_retry, metrica_payload)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
