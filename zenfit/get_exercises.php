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

$sql = "SELECT id, name, reps, sets, weight, rest_time, completed_sets, is_favorite, created_at 
        FROM exercises 
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
    $exercises = array();

    while ($row = $result->fetch_assoc()) {
        $exercises[] = array(
            'id' => intval($row['id']),
            'name' => $row['name'],
            'reps' => intval($row['reps']),
            'sets' => intval($row['sets']),
            'weight' => intval($row['weight']),
            'rest_time' => intval($row['rest_time']),
            'completed_sets' => intval($row['completed_sets']),
            'is_favorite' => boolval($row['is_favorite']),
            'created_at' => $row['created_at']
        );
    }

    echo json_encode(['success' => true, 'exercises' => $exercises]);
} else {
    echo json_encode(['success' => false, 'message' => 'Query failed: ' . $stmt->error]);
}

$stmt->close();
$conn->close();
?>
