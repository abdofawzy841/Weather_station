parent_directory="./parquet_files"
echo "refreshing indexes for today's files"
while true; do
  for directory in "$parent_directory"/*; do
    if [ -d "$directory" ]; then
      # Extract the directory name
      dir_name=$(basename "$directory")
      today=$(date +'%d-%m-%Y')
            # Find all files starting with today's date
      parquet_files=("$directory/$today"*)
      # Check if any Parquet files exist
      if [ ${#parquet_files[@]} -gt 0 ]; then
        echo "Updating index for ${parquet_files[@]}"
        elasticsearch_loader --index "$dir_name" --type "incident" --id-field "s_no" parquet "${parquet_files[@]}"
      else
        echo "Parquet files not found in directory: $directory"
      fi
    fi
  done
  echo "Sleeping for 5 minutes"
  sleep 300
done
