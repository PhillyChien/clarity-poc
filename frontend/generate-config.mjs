// generate-config.mjs
import fs from 'fs';
import path from 'path';
import dotenv from 'dotenv';

// Read the .env.local file
const envFilePath = path.resolve(process.cwd(), '.env.local');

// Check if the file exists
if (fs.existsSync(envFilePath)) {
  console.log(`Loading environment variables from: ${envFilePath}`);
  dotenv.config({ path: envFilePath });
} else {
  console.warn(`Warning: Environment file not found at ${envFilePath}. Using system environment variables only.`);
  // You can choose to load the default .env file here
  // dotenv.config();
}

const config = {};
console.log('Generating config.js for development...');

// Collect all environment variables starting with VITE_ (from .env file or system environment)
for (const key in process.env) {
  if (key.startsWith('VITE_')) {
    config[key] = process.env[key];
    console.log(`  Added ${key}`);
  }
}

const outputFile = path.resolve(process.cwd(), 'public/config.js');
// Use JSON.stringify to ensure the format is correct
const outputContent = `window._env_ = ${JSON.stringify(config, null, 2)};`;

try {
  fs.writeFileSync(outputFile, outputContent);
  console.log(`Development config written to ${outputFile}`);
  console.log('Content:', outputContent); // 取消註解以查看檔案內容
} catch (error) {
  console.error(`Error writing config file ${outputFile}:`, error);
  process.exit(1);
}