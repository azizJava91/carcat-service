$bodyPath = Join-Path $env:TEMP "webhook-body-pretty.json"
$secret = "Y2FybGFuZC1zZWNyZXQta2V5LXNoYXJlZC13aXRoLWh5cGVy"

$body = @'
{
    "plate": "99-FH-032",
    "vin": "3FA6P0HDXKR168752",
    "brand": "Ford",
    "model": "Fusion",
    "year": 2019,
    "engineVolume": 1.5,
    "engineType": null,
    "bodyType": null,
    "trim": null,
    "currentMileage": 121000,
    "serviceHistory": [
        {
            "recordId": 19387,
            "serviceType": "Mühərrik xidməti",
            "serviceGroups": [
                "Mühərrik xidməti",
                "Salon xidməti"
            ],
            "lastServiceDate": "2026-05-25",
            "lastServiceMileage": 121000,
            "services": [
                {
                    "serviceCode": 7,
                    "serviceName": "EXTRA Mühərrik yağının dəyişdirilməsi",
                    "serviceGroups": [
                        "Mühərrik xidməti"
                    ],
                    "universalServiceId": "Engine oil & filter",
                    "cost": {
                        "amount": 0,
                        "currency": "AZN"
                    },
                    "nextServiceDate": null,
                    "nextServiceMileage": null
                }
            ],
            "parts": [
                {
                    "name": "5W-20",
                    "qty": 0.8,
                    "unit": null
                }
            ],
            "cost": {
                "amount": 47.4,
                "currency": "AZN"
            },
            "finalCost": {
                "amount": 47.4,
                "currency": "AZN"
            },
            "nextServiceDate": null,
            "nextServiceMileage": null,
            "invoiceNumber": null,
            "dealer": "Babək Ekspress"
        }
    ]
}
'@

[System.IO.File]::WriteAllText($bodyPath, $body, [System.Text.UTF8Encoding]::new($false))

$bodyBytes = [System.IO.File]::ReadAllBytes($bodyPath)
$hmac = New-Object System.Security.Cryptography.HMACSHA256
$hmac.Key = [Text.Encoding]::UTF8.GetBytes($secret)
$sig = -join ($hmac.ComputeHash($bodyBytes) | ForEach-Object { $_.ToString("x2") })

Write-Host "Body file : $bodyPath"
Write-Host "Body bytes: $($bodyBytes.Length)"
Write-Host "Signature : $sig"
Write-Host ""

curl.exe -v -X POST "https://digital-innovation.agency/webhook/partner/new-service-visit" `
  -H "Content-Type: application/json" `
  -H "X-Signature: $sig" `
  --data-binary "@$bodyPath"
