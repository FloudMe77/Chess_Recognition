FROM nvidia/cuda:12.1.1-cudnn8-runtime-ubuntu22.04

ENV PYTHONDONTWRITEBYTECODE=1
ENV PYTHONUNBUFFERED=1
ENV DEBIAN_FRONTEND=noninteractive
WORKDIR /app

RUN apt-get update && apt-get install -y \
    software-properties-common curl git build-essential \
    libgl1 \
    && apt-get update && apt-get install -y \
    python3.11 python3.11-venv python3.11-distutils python3.11-dev \
    && rm -rf /var/lib/apt/lists/*

# Python 3.11 as defoult
RUN update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.11 2
RUN curl -sS https://bootstrap.pypa.io/get-pip.py | python3.11

RUN curl -sSL https://install.python-poetry.org | python3.11 -
ENV PATH="/root/.local/bin:$PATH"

COPY . /app

RUN poetry install

EXPOSE 8765

CMD ["poetry", "run", "uvicorn", "server:app", "--reload", "--host", "0.0.0.0", "--port", "8765"]
