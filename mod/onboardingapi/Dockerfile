FROM python:3.7-alpine

ARG UID=1000
ARG GID=1000

ENV SERVER_URL=https://git.onap.org/dcaegen2/platform/plain/mod
ENV PATH_COMPONENT_SPEC=/component-json-schemas/component-specification/dcae-cli-v2/component-spec-schema.json
ENV PATH_DATA_FORMAT=/component-json-schemas/data-format/dcae-cli-v1/data-format-schema.json
ENV PG_CONN=

COPY . /code
WORKDIR /code
RUN apk update && \
    apk add --virtual build-deps gcc python3-dev musl-dev && \
    apk add postgresql-dev bash && \
    addgroup -g $GID dcaemod && \
    adduser -s /bin/bash -u $UID -G dcaemod -D dcaemod && \
    mkdir schemas && \
    wget -O schemas/compspec.json $SERVER_URL$PATH_COMPONENT_SPEC && \
    wget -O schemas/dataformat.json $SERVER_URL$PATH_DATA_FORMAT && \
    pip install .

EXPOSE 8080
USER dcaemod

CMD /code/start.sh
