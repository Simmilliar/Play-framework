#!/bin/bash 
response=$(wget 'query.yahooapis.com/v1/public/yql?q=select+%2A+from+htmlstring+where+url%3D%27https%3A%2F%2Fxakep.ru%2Fpage%2F1%27+and+%28xpath%3D%27%2F%2Fdiv%5Bcontains%28%40class%2C+%22bd-main%22%29%5D%2F%2Farticle%27%29&format=json&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys' -q -O - | jq '.query.results.result' -r)
response=${response//\\\"/\"}
response=${response//\\n/}
response="<list>$response</list>"
response=$(echo $response | xml2json | jq '.list.article[] | if (.div[0].a | type) == "array" then .div[0].a[1].img.src else .div[0].a.img.src end, .div[1].header.h3.a.span["$t"], .div[1].p["$t"], if (.div[0].a | type) == "array" then .div[0].a[1].href else .div[0].a.href end' -r)
response=${response//\'/\'\'}
query="";
while read imgurl; do
	read title;
	read content;
	read url
	query="$query insert into news(id, title, description, url, image_url) values (uuid_generate_v4(), '$title', '$content', '$url', '$imgurl');"
done <<< "$response"
echo $query | psql -h localhost -p 5432 -U playuser playdb
exit 0