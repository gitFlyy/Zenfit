<?php
// filepath: d:\SMD\ZenFit\zenfit\get_meal_ideas.php
header('Content-Type: application/json; charset=utf-8');
require_once 'config.php';

$response = array();

$sql = "SELECT id, name, calories, carbs, protein, image FROM meal_ideas ORDER BY id DESC";
$result = $conn->query($sql);

if (!$result) {
    http_response_code(500);
    echo json_encode(array("error" => "Database query failed", "details" => $conn->error));
    exit;
}

$rows = array();
while ($row = $result->fetch_assoc()) {
    // Ensure image is returned as-is (base64 or null)
    if ($row['image'] === null || $row['image'] === '') {
        $row['image'] = null;
    }
    // cast numeric fields
    $row['id'] = (int)$row['id'];
    $row['calories'] = (int)$row['calories'];
    $rows[] = $row;
}

echo json_encode($rows);

$conn->close();
?>
