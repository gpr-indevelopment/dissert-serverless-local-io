FROM public.ecr.aws/lambda/nodejs:18

RUN yum -y install fio
COPY app.mjs package*.json ./

RUN npm ci --production

CMD ["app.lambdaHandler"]
