<?php
header('Content-Type: application/json');
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

$sql = "SELECT id, name, duration, reps, sets, weight, rest_time, created_at 
        FROM workouts 
        WHERE user_id = ? 
        ORDER BY created_at DESC";
$stmt = $conn->prepare($sql);

if (!$stmt) {
    echo json_encode(['success' => false, 'message' => 'DB prepare failed: ' . $conn->error]);
    exit;
}

$stmt->bind_param('s', $user_id);

if ($stmt->execute()) {
    $result = $stmt->get_result();
    $workouts = array();

    while ($row = $result->fetch_assoc()) {
        $workouts[] = array(
            'id' => intval($row['id']),
            'name' => $row['name'],
            'duration' => intval($row['duration']),
            'reps' => intval($row['reps']),
            'sets' => intval($row['sets']),
            'weight' => intval($row['weight']),
            'rest_time' => intval($row['rest_time']),
            'created_at' => $row['created_at']
        );
    }

    echo json_encode(['success' => true, 'workouts' => $workouts]);
} else {
    echo json_encode(['success' => false, 'message' => 'Query failed: ' . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
