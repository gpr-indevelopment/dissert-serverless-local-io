$headers = New-Object "System.Collections.Generic.Dictionary[[String],[String]]"
$headers.Add("Content-Type", "application/json")

$body = "{
`n    `"command`": `"if=/dev/zero of=/tmp/file1 bs=1024k count=1k`"
`n}"

$response = Invoke-RestMethod 'https://southamerica-east1-dissertacao-403601.cloudfunctions.net/gcf-dd' -Method 'POST' -Headers $headers -Body $body
$response | ConvertTo-Json