FROM gcr.io/distroless/java21-debian12

ENV TZ="Europe/Oslo"

COPY app/build/install/* /

USER nobody
CMD ["app"]
