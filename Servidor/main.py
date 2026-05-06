from fastapi import FastAPI, Depends, HTTPException, Header
from sqlalchemy import create_engine, Column, Integer, Float, String, DateTime
from sqlalchemy.ext.declarative import declarative_base
from sqlalchemy.orm import sessionmaker, Session
from pydantic import BaseModel
from datetime import datetime
from dotenv import load_dotenv
from datetime import date
import os

# Cargar variables de entorno
load_dotenv()
DATABASE_URL = os.getenv("DATABASE_URL")
SECRET_KEY = os.getenv("SECRET_KEY")

# Configurar base de datos
engine = create_engine(DATABASE_URL)
SessionLocal = sessionmaker(autocommit=False, autoflush=False, bind=engine)
Base = declarative_base()

# Modelo de base de datos
class Location(Base):
    __tablename__ = "locations"
    id = Column(Integer, primary_key=True, index=True)
    device_id = Column(String, index=True)
    latitude = Column(Float)
    longitude = Column(Float)
    accuracy = Column(Float)
    speed = Column(Float)
    battery = Column(Integer)
    timestamp = Column(DateTime, default=datetime.utcnow)

# Crear tablas
Base.metadata.create_all(bind=engine)

# Schema de entrada
class LocationCreate(BaseModel):
    device_id: str
    latitude: float
    longitude: float
    accuracy: float = 0.0
    speed: float = 0.0
    battery: int = 0

# Dependencia de BD
def get_db():
    db = SessionLocal()
    try:
        yield db
    finally:
        db.close()

# Autenticación por API Key
def verify_api_key(x_api_key: str = Header(...)):
    if x_api_key != SECRET_KEY:
        raise HTTPException(status_code=401, detail="API Key inválida")

# Iniciar FastAPI
app = FastAPI(title="GPS Tracker API")

# ── Endpoints ──────────────────────────────────────────

@app.get("/")
def root():
    return {"status": "GPS Server corriendo ✅"}

@app.post("/api/location")
def save_location(
    data: LocationCreate,
    db: Session = Depends(get_db),
    _: None = Depends(verify_api_key)
):
    loc = Location(**data.model_dump())
    db.add(loc)
    db.commit()
    db.refresh(loc)
    return {"message": "Ubicación guardada", "id": loc.id}

@app.get("/api/locations/{device_id}")
def get_locations(
    device_id: str,
    db: Session = Depends(get_db),
    _: None = Depends(verify_api_key)
):
    locs = db.query(Location).filter(
        Location.device_id == device_id
    ).order_by(Location.timestamp.desc()).limit(100).all()
    return locs

@app.get("/api/devices")
def get_devices(
    db: Session = Depends(get_db),
    _: None = Depends(verify_api_key)
):
    devices = db.query(Location.device_id).distinct().all()
    return [d[0] for d in devices]

@app.get("/api/locations/{device_id}/date/{fecha}")
def get_locations_by_date(
    device_id: str,
    fecha: str,
    db: Session = Depends(get_db),
    hora_inicio: str = "00:00",
    hora_fin: str = "23:59",
    _: None = Depends(verify_api_key)
):
    from datetime import datetime
    try:
        day_start = datetime.strptime(f"{fecha} {hora_inicio}", "%Y-%m-%d %H:%M")
        day_end   = datetime.strptime(f"{fecha} {hora_fin}",    "%Y-%m-%d %H:%M")
    except ValueError:
        raise HTTPException(status_code=400, detail="Formato inválido")

    locs = db.query(Location).filter(
        Location.device_id == device_id,
        Location.timestamp >= day_start,
        Location.timestamp <= day_end
    ).order_by(Location.timestamp.asc()).all()
    return locs

@app.get("/api/locations/{device_id}/latest")
def get_latest_location(
    device_id: str,
    db: Session = Depends(get_db),
    _: None = Depends(verify_api_key)
):
    loc = db.query(Location).filter(
        Location.device_id == device_id
    ).order_by(Location.timestamp.desc()).first()
    if not loc:
        raise HTTPException(status_code=404, detail="Dispositivo no encontrado")
    return loc

@app.delete("/api/locations/clear/{device_id}")
def clear_locations(
    device_id: str,
    db: Session = Depends(get_db),
    _: None = Depends(verify_api_key)
):
    if device_id == "ALL":
        count = db.query(Location).count()
        db.query(Location).delete()
    else:
        count = db.query(Location).filter(Location.device_id == device_id).count()
        db.query(Location).filter(Location.device_id == device_id).delete()
    db.commit()
    return {"message": f"{count} registros eliminados"}
