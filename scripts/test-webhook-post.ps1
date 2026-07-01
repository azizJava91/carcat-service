$bodyPath = Join-Path $env:TEMP "webhook-body.json"
$secret = "Y2FybGFuZC1zZWNyZXQta2V5LXNoYXJlZC13aXRoLWh5cGVy"

$body = '{"vin":"HHGHHHJHGHHHHHGGG","source":"hyper","items":[{"partnerRecordId":33333,"type":"Maintenance","date":"2026-06-15","mileage":52000,"serviceCenterId":1,"serviceCenterName":"HyperService","dealer":"Test Dealer","amount":{"amount":85.00,"currency":"AZN"},"serviceGroups":["Engine"],"services":[{"serviceCode":55555,"universalServiceId":"Engine oil & filter","serviceName":"Engine oil & filter","cost":{"amount":85.00,"currency":"AZN"},"nextServiceDate":"2027-06-15","nextServiceMileage":72000}],"parts":[{"name":"Engine oil filter","qty":1,"unit":"pc"}]}]}'

[System.IO.File]::WriteAllText($bodyPath, $body, [System.Text.UTF8Encoding]::new($false))

$bodyBytes = [System.IO.File]::ReadAllBytes($bodyPath)
$hmac = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key = [Text.Encoding]::UTF8.GetBytes($secret)
$sig = -join ($hmac.ComputeHash($bodyBytes) | ForEach-Object { $_.ToString("x2") })

Write-Host "Body file : $bodyPath"
Write-Host "Body bytes: $($bodyBytes.Length)"
Write-Host "Signature : $sig"
Write-Host ""

curl.exe -v -X POST "https://digital-innovation.agency/webhook/partner/edit/service-visit" `
  -H "Content-Type: application/json" `
  -H "X-Signature: $sig" `
  --data-binary "@$bodyPath"
