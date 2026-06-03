import os
from dotenv import load_dotenv

load_dotenv()

# RabbitMQ
RABBITMQ_HOST = os.getenv("RABBITMQ_HOST", "localhost")
RABBITMQ_PORT = int(os.getenv("RABBITMQ_PORT", "5672"))
RABBITMQ_USER = os.getenv("RABBITMQ_USER", "guest")
RABBITMQ_PASS = os.getenv("RABBITMQ_PASS", "guest")

# Crawl Sources
CRAWL_SOURCES = {
    "eastmoney": "https://www.eastmoney.com/",
    "tonghuashun": "https://www.10jqka.com.cn/",
    "xueqiu": "https://xueqiu.com/",
}

# Crawl Config
CRAWL_INTERVAL_MINUTES = 60
REQUEST_DELAY_MIN = 1.0
REQUEST_DELAY_MAX = 3.0
USER_AGENTS = [
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36",
]

# Dedup Config
DEDUP_WINDOW_HOURS = 24
SIMHASH_THRESHOLD = 3
