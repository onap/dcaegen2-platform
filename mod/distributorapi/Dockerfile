FROM python:3.7-alpine

COPY . /code
WORKDIR /code
RUN pip install .
EXPOSE 80
ENV DISTRIBUTOR_DEBUG=0
CMD start-distributor-api
