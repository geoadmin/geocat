#!/usr/bin/env python
import csv
from email.mime.text import MIMEText
from email.mime.multipart import MIMEMultipart
import json
import os
import requests
from requests.auth import HTTPBasicAuth
import smtplib
import StringIO


FIELDS = ["timeprocessed", "clientip", "request", "geoip.postal_code", "geoip.city_name", "agent"]
FROM_ADDR = 'patrick.valsecchi@camptocamp.com'
TO_ADDR = 'geocat2@camptocamp.com'


def get_cols(record):
    fields = record["fields"]
    return [fields[field][0] if field in fields else "-" for field in FIELDS]


def main():
    data = {
        "query": {
            "filtered": {
                "query": {"bool": {"should": [{
                    "query_string": {
                        "query": "*"
                    }
                }]}},
                "filter": {"bool": {
                    "must": [
                        {"fquery": {
                            "query": {"query_string": {"query": "request:(\"*md.delete?id*\")"}},
                            "_cache": True
                        }},
                        {"fquery": {
                            "query": {"query_string": {"query": "response:(200)"}},
                            "_cache": True
                        }}
                    ],
                    "must_not": [
                        {"fquery": {
                            "query": {"query_string": {
                                "query": "referrer.host:(\"tc-geocat.int.bgdi.ch\")"
                            }},
                            "_cache": True
                        }},
                        {"fquery": {
                            "query": {"query_string": {
                                "query": "referrer.host:(\"tc-geocat.dev.bgdi.ch\")"
                            }},
                            "_cache": True
                        }}
                    ]
                }}
            }
        },
        "size": 500,
        "sort": [
            {"timeprocessed": {"order": "asc"}},
            {"@timestamp": {"order": "asc"}}
        ],
        "fields": FIELDS
    }

    auth = HTTPBasicAuth(os.environ["USER"], os.environ["PASSWORD"])

    headers = {
        "Accept": "application/json",
        "Content-Type": "application/json;charset=utf-8"
    }

    r = requests.post("https://logs.bgdi.ch/elasticsearch/logstash-*/_search",
                      data=json.dumps(data), auth=auth, headers=headers)
    r.raise_for_status()

    csv_file = StringIO.StringIO()
    csv_writer = csv.writer(csv_file)
    csv_writer.writerow(FIELDS)
    for record in r.json["hits"]["hits"]:
        csv_writer.writerow(get_cols(record))

    csv_file.seek(0)

    msg = MIMEMultipart()
    msg['Subject'] = '[sb415] Deleted MDs'
    msg['From'] = FROM_ADDR
    msg['To'] = TO_ADDR
    msg.preamble = 'List of deleted MDs during the last 30 days'

    mime_text = MIMEText(csv_file.read(), 'csv', 'utf-8')
    mime_text.add_header('Content-Disposition', 'attachment', filename="deleted.csv")
    msg.attach(mime_text)

    s = smtplib.SMTP('localhost')
    s.sendmail(FROM_ADDR, TO_ADDR, msg.as_string())
    s.quit()

main()
