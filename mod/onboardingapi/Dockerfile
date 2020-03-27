FROM python:3.7-alpine

ARG UID=1000
ARG GID=1000

COPY . /code
WORKDIR /code
RUN apk update && \
    apk add --virtual build-deps gcc python-dev musl-dev && \
    apk add postgresql-dev bash && \
    addgroup -g $GID dcaemod && \
    adduser -s /bin/bash -u $UID -G dcaemod -D dcaemod && \
    pip install .

EXPOSE 8080
USER dcaemod

CMD /code/start.sh
