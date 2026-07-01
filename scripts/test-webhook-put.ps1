$bodyPath = Join-Path $env:TEMP "webhook-put-body.json"
$secret = "Y2FybGFuZC1zZWNyZXQta2V5LXNoYXJlZC13aXRoLWh5cGVy"

$body = '{"vin":"HHGHHHJHGHHHHHGGG","partnerRecordId":33333,"mileage":53000,"services":[{"serviceCode":55555,"serviceName":"Engine oil & filter changed","cost":{"amount":95.00,"currency":"AZN"},"nextServiceMileage":75000}]}'

[System.IO.File]::WriteAllText($bodyPath, $body, [System.Text.UTF8Encoding]::new($false))

$bodyBytes = [System.IO.File]::ReadAllBytes($bodyPath)
$hmac = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key = [Text.Encoding]::UTF8.GetBytes($secret)
$sig = -join ($hmac.ComputeHash($bodyBytes) | ForEach-Object { $_.ToString("x2") })

Write-Host "Body file : $bodyPath"
Write-Host "Signature : $sig"
Write-Host ""

curl.exe -v -X PUT "https://digital-innovation.agency/webhook/partner/edit/service-visit" `
  -H "Content-Type: application/json" `
  -H "X-Signature: $sig" `
  --data-binary "@$bodyPath"
