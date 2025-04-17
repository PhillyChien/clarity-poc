#!/bin/sh

# The root directory of the Nginx service (consistent with the Dockerfile)
NGINX_ROOT_DIR=/usr/share/nginx/html
# The path to the JavaScript configuration file to be generated
CONFIG_JS_PATH=${NGINX_ROOT_DIR}/config.js

# Start writing the JS file, mount the variables to the window._env_ object
echo "window._env_ = {" > $CONFIG_JS_PATH

# Read all variables starting with VITE_ from environment variables
printenv | grep '^VITE_' | while read -r line; do
  # Split the variable name and value (handle cases where the value contains '=')
  varname=$(echo "$line" | cut -d= -f1)
  varvalue=$(echo "$line" | cut -d= -f2-)

  # Clean/escape the value of the variable to be correctly placed in the JS string
  # For complex values, a more robust JSON escape may be needed
  escaped_value=$(echo "$varvalue" | sed 's/\\/\\\\/g' | sed 's/"/\\"/g')

  # Write the JS object property
  echo "  $varname: \"$escaped_value\"," >> $CONFIG_JS_PATH
done

echo "}" >> $CONFIG_JS_PATH

echo "Generated runtime config at $CONFIG_JS_PATH:"
cat $CONFIG_JS_PATH # Output the generated file content in the Log for debugging

# Start the Nginx service (running in the foreground)
# Use exec to replace the script process with the Nginx process, making it the main process of the container
echo "Starting Nginx..."
exec nginx -g 'daemon off;'