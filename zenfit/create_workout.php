<?php
header('Content-Type: application/json');
include 'config.php';

if ($_SERVER['REQUEST_METHOD'] !== 'POST') {
    echo json_encode(['success' => false, 'message' => 'Invalid request method']);
    exit;
}

$user_id = isset($_POST['user_id']) ? trim($_POST['user_id']) : '';
$name = isset($_POST['name']) ? trim($_POST['name']) : '';
$duration = isset($_POST['duration']) ? intval($_POST['duration']) : 1800; // Default 30 minutes in seconds
$reps = isset($_POST['reps']) ? intval($_POST['reps']) : 0;
$sets = isset($_POST['sets']) ? intval($_POST['sets']) : 0;
$weight = isset($_POST['weight']) ? intval($_POST['weight']) : 0;
$rest_time = isset($_POST['rest_time']) ? intval($_POST['rest_time']) : 60;

if (empty($user_id) || empty($name)) {
    echo json_encode(['success' => false, 'message' => 'Missing required fields']);
    exit;
}

$sql = "INSERT INTO workouts (user_id, name, duration, reps, sets, weight, rest_time, created_at) 
        VALUES (?, ?, ?, ?, ?, ?, ?, NOW())";
$stmt = $conn->prepare($sql);

if (!$stmt) {
    echo json_encode(['success' => false, 'message' => 'DB prepare failed: ' . $conn->error]);
    exit;
}

$stmt->bind_param('ssiiiii', $user_id, $name, $duration, $reps, $sets, $weight, $rest_time);

if ($stmt->execute()) {
    echo json_encode(['success' => true, 'message' => 'Workout created successfully']);
} else {
    echo json_encode(['success' => false, 'message' => 'Failed to create workout: ' . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
