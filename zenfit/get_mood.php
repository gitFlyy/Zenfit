<?php
include "config.php";
header("Content-Type: application/json");

$response = [];

if ($_SERVER['REQUEST_METHOD'] == 'POST') {

    $user_id = $_POST['user_id'];

    $stmt = $conn->prepare("SELECT mood FROM mood WHERE user_id=? ORDER BY created_at DESC LIMIT 1");
    $stmt->bind_param("i", $user_id);
    $stmt->execute();
    $result = $stmt->get_result();

    if ($row = $result->fetch_assoc()) {
        $response["success"] = true;
        $response["mood"] = $row["mood"];
    } else {
        $response["success"] = false;
        $response["mood"] = "None";
    }

    echo json_encode($response);
}
?>
