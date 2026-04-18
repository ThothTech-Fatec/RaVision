import os
from fastapi import FastAPI, HTTPException, Request
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
async def chat_endpoint(request: ChatRequest, req: Request):
    """
    Endpoint principal que recebe a mensagem do usuário e retorna a explicação da IA.
    """
    if not request.message:
        raise HTTPException(status_code=400, detail="Mensagem vazia")
    
    auth_header = req.headers.get("Authorization")
    
    try:
        response_text = await process_user_query(request.message, request.dateRef, auth_header)
        return ChatResponse(response=response_text)
    except Exception as e:
        print(f"Erro no endpoint de chat: {e}")
        raise HTTPException(status_code=500, detail="Erro interno ao processar chat")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8001)
