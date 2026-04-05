import os
from dotenv import load_dotenv
from langchain_huggingface import HuggingFaceEndpoint

load_dotenv()

def test_llm():
    token = os.getenv("HUGGINGFACEHUB_API_TOKEN")
    print(f"Token length: {len(token) if token else 0}")
    
    llm = HuggingFaceEndpoint(
        repo_id="meta-llama/Llama-3.3-70B-Instruct",
        task="text-generation",
        max_new_tokens=100,
        temperature=0.1,
        huggingfacehub_api_token=token
    )
    
    try:
        response = llm.invoke("Hello, are you working?")
        print(f"Response: {response}")
    except Exception as e:
        print(f"Error calling LLM: {e}")

if __name__ == "__main__":
    test_llm()
