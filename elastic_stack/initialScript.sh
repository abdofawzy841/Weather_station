parent_directory="./parquet_files"
echo "Building indexes for all directories in $parent_directory"
for directory in "$parent_directory"/*; do
  if [ -d "$directory" ]; then
    # Extract the directory name
    dir_name=$(basename "$directory")

    # Run the elasticsearch_loader command for the current directory
    echo "Building index for $directory"

    # Create the index in Elasticsearch
    elasticsearch_loader --index "$dir_name" --type "incident" --delete --id-field "s_no" parquet $directory/*

    # Create/update the index pattern in Kibana
    curl -XPOST -H 'Content-Type: application/json' -H 'kbn-xsrf: true' -d '{
      "attributes": {
        "title": "'"$dir_name"'",
        "timeFieldName": "timestamp"
      }
    }' "http://localhost:5601/api/saved_objects/index-pattern/$dir_name-*"

    curl -XPOST -H 'Content-Type: application/json' -H 'kbn-xsrf: true' -d '{
      "name": "timestamp",
      "runtimeField": {
        "type": "date",
        "script": {
          "source": "emit(doc[\"status_timestamp\"].value);"
        }
      }
    }' "http://localhost:5601/api/index_patterns/index_pattern/$dir_name-*/runtime_field"
    
  fi
done

curl -XPOST -H 'Content-Type: application/json' -H 'kbn-xsrf: true' -d '{
  "attributes": {
    "title": "'"station*"'",
    "timeFieldName": "timestamp"
  }
}' "http://localhost:5601/api/saved_objects/index-pattern/station-*"

curl -XPOST -H 'Content-Type: application/json' -H 'kbn-xsrf: true' -d '{
  "name": "timestamp",
  "runtimeField": {
    "type": "date",
    "script": {
      "source": "emit(doc[\"status_timestamp\"].value);"
    }
  }
}' "http://localhost:5601/api/index_patterns/index_pattern/station-*/runtime_field"

curl -XPOST -H 'Content-Type: application/json' -H 'kbn-xsrf: true' -d '{
  "changes": {
    "defaultIndex": "station-*"
  }
}' "http://localhost:5601/api/kibana/settings"
