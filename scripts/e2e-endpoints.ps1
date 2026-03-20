$ErrorActionPreference = "Stop"
Add-Type -AssemblyName System.Net.Http

$client = [System.Net.Http.HttpClient]::new()
$internalApiKey = "inventory-internal-key"

function Send-Request {
    param(
        [Parameter(Mandatory = $true)][string]$Method,
        [Parameter(Mandatory = $true)][string]$Url,
        [string]$Token,
        [string]$Json,
        [hashtable]$ExtraHeaders
    )

    $httpMethod = switch ($Method.ToUpperInvariant()) {
        "GET" { [System.Net.Http.HttpMethod]::Get }
        "POST" { [System.Net.Http.HttpMethod]::Post }
        default { [System.Net.Http.HttpMethod]::new($Method.ToUpperInvariant()) }
    }

    $request = [System.Net.Http.HttpRequestMessage]::new($httpMethod, $Url)
    if ($Token) {
        $request.Headers.Authorization = [System.Net.Http.Headers.AuthenticationHeaderValue]::new("Bearer", $Token)
    }
    if ($ExtraHeaders) {
        foreach ($header in $ExtraHeaders.GetEnumerator()) {
            $request.Headers.Add($header.Key, [string]$header.Value)
        }
    }
    if ($PSBoundParameters.ContainsKey("Json")) {
        $content = if ($null -eq $Json) { "" } else { $Json }
        $request.Content = [System.Net.Http.StringContent]::new($content, [System.Text.Encoding]::UTF8, "application/json")
    }

    $response = $client.SendAsync($request).GetAwaiter().GetResult()
    $body = $response.Content.ReadAsStringAsync().GetAwaiter().GetResult()
    [pscustomobject]@{
        status = [int]$response.StatusCode
        body = $body
    }
}

function Add-Result {
    param(
        [System.Collections.Generic.List[object]]$Results,
        [string]$Name,
        [object]$Response
    )

    $Results.Add([pscustomobject]@{
        name = $Name
        status = $Response.status
        body = $Response.body
    })
}

$results = [System.Collections.Generic.List[object]]::new()
$seed = [DateTimeOffset]::UtcNow.ToUnixTimeMilliseconds()
$today = (Get-Date).ToString("yyyy-MM-dd")

$adminLogin = Send-Request -Method POST -Url "http://localhost:8081/api/auth/login" -Json '{"username":"admin","password":"Admin123!"}'
$operatorLogin = Send-Request -Method POST -Url "http://localhost:8081/api/auth/login" -Json '{"username":"operator","password":"Operator123!"}'
$adminToken = ($adminLogin.body | ConvertFrom-Json).token
$operatorToken = ($operatorLogin.body | ConvertFrom-Json).token

Add-Result -Results $results -Name "auth_login_admin" -Response $adminLogin
Add-Result -Results $results -Name "auth_login_operator" -Response $operatorLogin
Add-Result -Results $results -Name "auth_login_bad_credentials" -Response (Send-Request -Method POST -Url "http://localhost:8081/api/auth/login" -Json '{"username":"admin","password":"bad"}')
Add-Result -Results $results -Name "auth_logout_operator" -Response (Send-Request -Method POST -Url "http://localhost:8081/api/auth/logout" -Token $operatorToken)
Add-Result -Results $results -Name "auth_logout_no_token" -Response (Send-Request -Method POST -Url "http://localhost:8081/api/auth/logout")

$userBody = (@{
    username = "tester$seed"
    fullName = "Usuario Test"
    password = "Temp123!"
    role = "ROLE_OPERATOR"
} | ConvertTo-Json -Compress)

Add-Result -Results $results -Name "users_list_admin" -Response (Send-Request -Method GET -Url "http://localhost:8081/api/users" -Token $adminToken)
Add-Result -Results $results -Name "users_list_operator" -Response (Send-Request -Method GET -Url "http://localhost:8081/api/users" -Token $operatorToken)
Add-Result -Results $results -Name "users_list_no_token" -Response (Send-Request -Method GET -Url "http://localhost:8081/api/users")
Add-Result -Results $results -Name "users_create_admin" -Response (Send-Request -Method POST -Url "http://localhost:8081/api/users" -Token $adminToken -Json $userBody)
Add-Result -Results $results -Name "users_create_operator" -Response (Send-Request -Method POST -Url "http://localhost:8081/api/users" -Token $operatorToken -Json $userBody)

$categoryBody = (@{ name = "Categoria-$seed"; description = "Categoria de prueba" } | ConvertTo-Json -Compress)
$categoryBody2 = (@{ name = "Categoria-$seed-2"; description = "Categoria para producto" } | ConvertTo-Json -Compress)

Add-Result -Results $results -Name "categories_list_admin" -Response (Send-Request -Method GET -Url "http://localhost:8082/api/categories" -Token $adminToken)
Add-Result -Results $results -Name "categories_list_operator" -Response (Send-Request -Method GET -Url "http://localhost:8082/api/categories" -Token $operatorToken)
Add-Result -Results $results -Name "categories_list_no_token" -Response (Send-Request -Method GET -Url "http://localhost:8082/api/categories")
Add-Result -Results $results -Name "categories_create_admin" -Response (Send-Request -Method POST -Url "http://localhost:8082/api/categories" -Token $adminToken -Json $categoryBody)
Add-Result -Results $results -Name "categories_create_operator" -Response (Send-Request -Method POST -Url "http://localhost:8082/api/categories" -Token $operatorToken -Json $categoryBody)
$categoryCreateAdmin2 = Send-Request -Method POST -Url "http://localhost:8082/api/categories" -Token $adminToken -Json $categoryBody2
$categoryId = ($categoryCreateAdmin2.body | ConvertFrom-Json).id
Add-Result -Results $results -Name "categories_create_admin_2" -Response $categoryCreateAdmin2

$productBody = (@{
    name = "Producto-$seed"
    sku = "SKU-$seed"
    description = "Producto de prueba"
    categoryId = $categoryId
    unitPrice = 10.5
} | ConvertTo-Json -Compress)
$productBody2 = (@{
    name = "Producto-$seed-2"
    sku = "SKU-$seed-2"
    description = "Producto para inventario"
    categoryId = $categoryId
    unitPrice = 20
} | ConvertTo-Json -Compress)

Add-Result -Results $results -Name "products_list_admin" -Response (Send-Request -Method GET -Url "http://localhost:8082/api/products" -Token $adminToken)
Add-Result -Results $results -Name "products_list_operator" -Response (Send-Request -Method GET -Url "http://localhost:8082/api/products" -Token $operatorToken)
Add-Result -Results $results -Name "products_create_admin" -Response (Send-Request -Method POST -Url "http://localhost:8082/api/products" -Token $adminToken -Json $productBody)
Add-Result -Results $results -Name "products_create_operator" -Response (Send-Request -Method POST -Url "http://localhost:8082/api/products" -Token $operatorToken -Json $productBody)
$productCreateAdmin2 = Send-Request -Method POST -Url "http://localhost:8082/api/products" -Token $adminToken -Json $productBody2
$productId = ($productCreateAdmin2.body | ConvertFrom-Json).id
Add-Result -Results $results -Name "products_create_admin_2" -Response $productCreateAdmin2
Add-Result -Results $results -Name "internal_product_validation_admin_direct_forbidden" -Response (Send-Request -Method GET -Url "http://localhost:8082/api/internal/products/$productId/validation" -Token $adminToken)
Add-Result -Results $results -Name "internal_categories_admin_direct_forbidden" -Response (Send-Request -Method GET -Url "http://localhost:8082/api/internal/categories" -Token $adminToken)
Add-Result -Results $results -Name "internal_product_validation_with_key" -Response (Send-Request -Method GET -Url "http://localhost:8082/api/internal/products/$productId/validation" -Token $adminToken -ExtraHeaders @{ "X-Internal-Api-Key" = $internalApiKey })
Add-Result -Results $results -Name "internal_categories_with_key" -Response (Send-Request -Method GET -Url "http://localhost:8082/api/internal/categories" -Token $adminToken -ExtraHeaders @{ "X-Internal-Api-Key" = $internalApiKey })

$entryOperatorBody = (@{
    productId = $productId
    quantity = 25
    reference = "ENT-001"
    destination = "Bodega central"
    notes = "Compra inicial"
} | ConvertTo-Json -Compress)
$entryAdminBody = (@{
    productId = $productId
    quantity = 10
    reference = "ENT-002"
    destination = "Bodega central"
    notes = "Reposicion admin"
} | ConvertTo-Json -Compress)
$exitOperatorBody = (@{
    productId = $productId
    quantity = 8
    reference = "SAL-001"
    destination = "Cliente externo"
    notes = "Salida de prueba"
} | ConvertTo-Json -Compress)
$exitOverBody = (@{
    productId = $productId
    quantity = 1000
    reference = "SAL-999"
    destination = "Ninguno"
    notes = "Salida invalida"
} | ConvertTo-Json -Compress)

Add-Result -Results $results -Name "stock_initial_admin" -Response (Send-Request -Method GET -Url "http://localhost:8083/api/stocks/$productId" -Token $adminToken)
Add-Result -Results $results -Name "entry_operator" -Response (Send-Request -Method POST -Url "http://localhost:8083/api/movements/entries" -Token $operatorToken -Json $entryOperatorBody)
Add-Result -Results $results -Name "entry_admin" -Response (Send-Request -Method POST -Url "http://localhost:8083/api/movements/entries" -Token $adminToken -Json $entryAdminBody)
Add-Result -Results $results -Name "exit_operator" -Response (Send-Request -Method POST -Url "http://localhost:8083/api/movements/exits" -Token $operatorToken -Json $exitOperatorBody)
Add-Result -Results $results -Name "exit_overstock_admin" -Response (Send-Request -Method POST -Url "http://localhost:8083/api/movements/exits" -Token $adminToken -Json $exitOverBody)
Add-Result -Results $results -Name "stock_after_admin" -Response (Send-Request -Method GET -Url "http://localhost:8083/api/stocks/$productId" -Token $adminToken)
Add-Result -Results $results -Name "movements_list_admin" -Response (Send-Request -Method GET -Url "http://localhost:8083/api/movements" -Token $adminToken)
Add-Result -Results $results -Name "movements_list_operator" -Response (Send-Request -Method GET -Url "http://localhost:8083/api/movements" -Token $operatorToken)
Add-Result -Results $results -Name "internal_stock_summary_admin_direct_forbidden" -Response (Send-Request -Method GET -Url "http://localhost:8083/api/internal/stocks-summary" -Token $adminToken)
Add-Result -Results $results -Name "internal_movements_summary_admin_direct_forbidden" -Response (Send-Request -Method GET -Url "http://localhost:8083/api/internal/movements-summary?from=$today&to=$today" -Token $adminToken)
Add-Result -Results $results -Name "internal_stock_summary_with_key" -Response (Send-Request -Method GET -Url "http://localhost:8083/api/internal/stocks-summary" -Token $adminToken -ExtraHeaders @{ "X-Internal-Api-Key" = $internalApiKey })
Add-Result -Results $results -Name "internal_movements_summary_with_key" -Response (Send-Request -Method GET -Url "http://localhost:8083/api/internal/movements-summary?from=$today&to=$today" -Token $adminToken -ExtraHeaders @{ "X-Internal-Api-Key" = $internalApiKey })

Add-Result -Results $results -Name "reports_stock_summary_admin" -Response (Send-Request -Method GET -Url "http://localhost:8084/api/reports/stock-summary" -Token $adminToken)
Add-Result -Results $results -Name "reports_stock_summary_operator" -Response (Send-Request -Method GET -Url "http://localhost:8084/api/reports/stock-summary" -Token $operatorToken)
Add-Result -Results $results -Name "reports_movements_summary_admin" -Response (Send-Request -Method GET -Url "http://localhost:8084/api/reports/movements-summary?from=$today&to=$today" -Token $adminToken)
Add-Result -Results $results -Name "reports_history_admin" -Response (Send-Request -Method GET -Url "http://localhost:8084/api/reports/history" -Token $adminToken)
Add-Result -Results $results -Name "reports_history_operator" -Response (Send-Request -Method GET -Url "http://localhost:8084/api/reports/history" -Token $operatorToken)
Add-Result -Results $results -Name "audit_history_admin" -Response (Send-Request -Method GET -Url "http://localhost:8084/api/audit/access-history" -Token $adminToken)
Add-Result -Results $results -Name "audit_history_operator" -Response (Send-Request -Method GET -Url "http://localhost:8084/api/audit/access-history" -Token $operatorToken)

$internalAuditBody = (@{ username = "probe"; role = "ROLE_ADMIN"; eventType = "LOGIN" } | ConvertTo-Json -Compress)
Add-Result -Results $results -Name "internal_audit_admin_direct_forbidden" -Response (Send-Request -Method POST -Url "http://localhost:8084/api/internal/access-events" -Token $adminToken -Json $internalAuditBody)
Add-Result -Results $results -Name "internal_audit_with_key" -Response (Send-Request -Method POST -Url "http://localhost:8084/api/internal/access-events" -Token $adminToken -Json $internalAuditBody -ExtraHeaders @{ "X-Internal-Api-Key" = $internalApiKey })

$results | ConvertTo-Json -Depth 5
