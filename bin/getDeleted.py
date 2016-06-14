#!/usr/bin/env python3
import csv
import json
import os
import requests
from requests.auth import HTTPBasicAuth

import sys

FIELDS = ["timeprocessed", "clientip", "request", "geoip.postal_code", "geoip.city_name", "agent"]


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

    csv_writer = csv.writer(sys.stdout)
    csv_writer.writerow(FIELDS)
    for record in r.json()["hits"]["hits"]:
        csv_writer.writerow(get_cols(record))


main()
