gcloud functions deploy gcf-shell --gen2 --runtime=nodejs18 --source=. --entry-point=gcf-shell --trigger-http --memory=500M --max-instances=10 --allow-unauthenticated