from fastapi import FastAPI

app = FastAPI(title="Crawler Service", version="1.0.0")


@app.get("/health")
def health():
    return {"status": "ok"}


@app.post("/crawl/trigger")
def trigger_crawl():
    return {"message": "crawl triggered", "count": 0}


@app.get("/crawl/status")
def crawl_status():
    return {"status": "idle", "last_crawl": None}
