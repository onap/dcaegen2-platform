FROM python:3.7-alpine

RUN apk update && \
    apk add --virtual build-deps gcc python-dev musl-dev && \
    apk add postgresql-dev bash
COPY . /code
WORKDIR /code
RUN pip install . \
    && mkdir -p ~/.config/dcae-cli
EXPOSE 80

CMD /code/start.sh
