FROM node:20-alpine

WORKDIR /app

COPY server/package*.json ./
RUN npm install --omit=dev && npm cache clean --force

COPY server/src ./src

ENV NODE_ENV=production
ENV PORT=3000

EXPOSE 3000

CMD ["node", "src/index.js"]
