from fastapi import FastAPI
import os

app = FastAPI(title="Vesti AI Service")

@app.get("/health")
def health_check():
    return {"status": "AI service is running"}

@app.post("/analyze")
def analyze_image(image_url: str):
    # Mock analysis
    return {
        "color": "Classic Soft Indigo",
        "category": "T-Shirt",
        "style": "Casual"
    }

@app.post("/outfit-suggestion")
def outfit_suggestion(user_id: str):
    return {"suggestion": "Mocked suggestion based on wardrobe"}

@app.post("/style-profile")
def style_profile(user_id: str):
    return {"profile": "Mocked style profile"}
