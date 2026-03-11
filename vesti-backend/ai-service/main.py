from fastapi import FastAPI, UploadFile, File, Form, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import List, Optional
import uvicorn
import os

app = FastAPI(title="Vesti AI Service", version="1.0.0")

# Enable CORS for frontend/mobile testing if necessary
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Dummy Models
class OutfitRecommendationRequest(BaseModel):
    user_id: str
    weather_condition: Optional[str] = "Sunny"
    temperature: Optional[int] = 25
    style_preference: Optional[str] = "Casual"

class WardrobeItem(BaseModel):
    id: str
    image_url: str
    category: str
    color: str

class RecommendationResponse(BaseModel):
    outfit_id: str
    description: str
    items: List[WardrobeItem]

@app.get("/health")
def health_check():
    return {"status": "AI service is running"}

@app.post("/api/ai/tag-image")
async def tag_image(image: UploadFile = File(...)):
    """
    Placeholder endpoint for ML Model integration.
    Right now it returns mock tags instead of running an actual inference model.
    """
    # TODO: Pass `image.file` to PyTorch/TensorFlow model
    
    # Mock Response
    return {
        "filename": image.filename,
        "predicted_category": "T-Shirt",
        "predicted_color": "Blue",
        "predicted_style": "Casual",
        "confidence_score": 0.95
    }

@app.post("/api/ai/recommend", response_model=RecommendationResponse)
def get_recommendation(request: OutfitRecommendationRequest):
    """
    Placeholder endpoint for Outfit Recommendation ML Engine.
    Uses generic logic or mock data instead of real AI until the model is trained.
    """
    # TODO: Fetch user's actual wardrobe items from wardrobe-service
    # TODO: Run recommendation algorithm/model
    
    # Mock Data
    mock_items = [
        WardrobeItem(
            id="item-123", 
            image_url="/uploads/mock-shirt.jpg", 
            category="T-Shirt", 
            color="White"
        ),
        WardrobeItem(
            id="item-456", 
            image_url="/uploads/mock-pants.jpg", 
            category="Jeans", 
            color="Blue"
        )
    ]
    
    desc = f"Based on {request.temperature}°C {request.weather_condition} weather, here is a {request.style_preference} outfit."
    
    return RecommendationResponse(
        outfit_id="outfit-mock-001",
        description=desc,
        items=mock_items
    )

if __name__ == "__main__":
    port = int(os.getenv("PORT", 8082))
    uvicorn.run("main:app", host="0.0.0.0", port=port, reload=True)
