# 1. Obraz bazowy z CUDA i Ubuntu 22.04
FROM nvidia/cuda:12.1.1-cudnn8-runtime-ubuntu22.04

# 2. Ustawienia środowiska
ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV DEBIAN_FRONTEND=noninteractive
WORKDIR /app

# 3. Instalacja Pythona 3.11 i narzędzi systemowych
RUN apt-get update && apt-get install -y \
    software-properties-common curl git build-essential \
    && add-apt-repository ppa:deadsnakes/ppa \
    && apt-get update && apt-get install -y \
    python3.11 python3.11-venv python3.11-distutils python3.11-dev \
    && rm -rf /var/lib/apt/lists/*

# Ustaw Python 3.11 jako domyślny
RUN update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.11 2
RUN curl -sS https://bootstrap.pypa.io/get-pip.py | python3.11

# 4. Instalacja Poetry
RUN curl -sSL https://install.python-poetry.org | python3 -
ENV PATH="/root/.local/bin:$PATH"

# 5. Skopiuj definicje projektu (dla zależności)
COPY pyproject.toml poetry.lock* /app/

# 6. Instalacja zależności (bez dev, bez root)
RUN poetry install --no-root --without dev

# 7. Skopiuj resztę kodu
COPY . /app

# 8. Otwórz port aplikacji
EXPOSE 8765

# 9. Domyślna komenda uruchamiająca serwer
CMD ["poetry", "run", "uvicorn", "server:app", "--reload", "--host", "0.0.0.0", "--port", "8765"]
