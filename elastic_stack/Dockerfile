FROM nshou/elasticsearch-kibana:kibana7

USER root

RUN apt-get update && apt-get install -y python3 python3-pip curl
RUN pip3 install 'elasticsearch-loader[parquet]'
RUN pip3 install elasticsearch==7.13.2  # Update the elasticsearch library version
RUN pip3 install python-snappy
RUN pip install --upgrade parquet


USER elasticsearch

COPY script.sh /usr/local/bin/script.sh
COPY . .

CMD ["bash", "-c", "elasticsearch-${EK_VERSION}/bin/elasticsearch -E http.host=0.0.0.0 --quiet & kibana-${EK_VERSION}-linux-x86_64/bin/kibana --allow-root --host 0.0.0.0 -Q & sleep 300 && /usr/local/bin/script.sh"]

EXPOSE 9200
EXPOSE 5601
