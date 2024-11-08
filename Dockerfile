FROM gcr.io/distroless/java21-debian12

ENV TZ='Europe/Oslo'
ENV LC_ALL='nb_NO.UTF-8'
ENV LANG='nb_NO.UTF-8'

WORKDIR /app

COPY app/build/install/app/lib/*.jar .

USER nobody

CMD ["app.jar"]
