<?php
header('Content-Type: application/json');

// Include your DB config (should set $conn)
include 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
    exit;
}

$user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';

if (empty($user_id)) {
    echo json_encode(['success' => false, 'message' => 'Invalid user ID']);
    exit;
}

$sql = "SELECT id, name, calories, carbs, protein, image_url, created_at FROM meals WHERE user_id = ? ORDER BY created_at DESC";
$stmt = $conn->prepare($sql);

if (!$stmt) {
    echo json_encode(['success' => false, 'message' => 'DB prepare failed: ' . $conn->error]);
    exit;
}

// user_id is string (s), not int (i)
$stmt->bind_param('s', $user_id);

if ($stmt->execute()) {
    $result = $stmt->get_result();
    $meals = array();

    while ($row = $result->fetch_assoc()) {
        $meals[] = array(
            'id' => $row['id'],
            'name' => $row['name'],
            'calories' => $row['calories'],
            'carbs' => $row['carbs'],
            'protein' => $row['protein'],
            'image_url' => $row['image_url'],
            'created_at' => $row['created_at']
        );
    }

    echo json_encode(['success' => true, 'meals' => $meals]);
} else {
    echo json_encode(['success' => false, 'message' => 'Query failed: ' . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
