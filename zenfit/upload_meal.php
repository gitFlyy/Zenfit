<?php
header('Content-Type: application/json');

// Include your DB config (should set $conn)
include 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
    exit;
}

$user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
$name = isset($_POST['name']) ? trim($_POST['name']) : '';
$calories = isset($_POST['calories']) ? intval($_POST['calories']) : null;
$carbs = isset($_POST['carbs']) ? trim($_POST['carbs']) : null;
$protein = isset($_POST['protein']) ? trim($_POST['protein']) : null;
$image_base64 = isset($_POST['image']) ? $_POST['image'] : null;

if (empty($user_id) || empty($name) || $calories === null) {
    echo json_encode(['success' => false, 'message' => 'Missing required fields']);
    exit;
}

$sql = "INSERT INTO meals (user_id, name, calories, carbs, protein, image_url) VALUES (?, ?, ?, ?, ?, ?)";
$stmt = $conn->prepare($sql);
if (!$stmt) {
    echo json_encode(['success' => false, 'message' => 'DB prepare failed: ' . $conn->error]);
    exit;
}

$stmt->bind_param('ssisss', $user_id, $name, $calories, $carbs, $protein, $image_base64);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Meal uploaded successfully']);
} else {
    echo json_encode(['success' => false, 'message' => 'Insert failed: ' . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
