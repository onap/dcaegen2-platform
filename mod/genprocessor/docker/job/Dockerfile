FROM maven:3-jdk-8

COPY . /code
WORKDIR /code
RUN mvn package dependency:copy-dependencies
ENV GENPROC_WORKING_DIR=/work
ENV GENPROC_ONBOARDING_API_HOST=http://onboarding-api/onboarding
ENV GENPROC_PROCESSOR_CLASSFILE_PATH=/code/target/classes/sandbox/DCAEProcessor.class
ENV GENPROC_SLEEP_SEC=10

ENV _RUN_COMMAND="java -cp \"target/genprocessor-1.0.1.jar:target/dependency/*\" sandbox.App gen"
RUN printf "#!/bin/bash\nwhile true\ndo\n\t$_RUN_COMMAND\n\tsleep $GENPROC_SLEEP_SEC\ndone" > /code/run.sh \
    & chmod +x /code/run.sh

CMD /code/run.sh
