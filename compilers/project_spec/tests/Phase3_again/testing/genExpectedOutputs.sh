for file in tests/*out
do
  cp "$file" "${file/out/res}"
done
