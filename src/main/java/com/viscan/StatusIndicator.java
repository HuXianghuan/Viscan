package com.viscan;

import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;

public class StatusIndicator {
    private Circle circle;

    public StatusIndicator() {
        circle = new Circle(8);
        setStatus("unknown");
    }


    public void setStatus(String status) {
        switch (status.toLowerCase()) {
            case "ok":
                circle.setFill(Color.LIMEGREEN);
                break;
            case "warning":
                circle.setFill(Color.GOLD);
                break;
            case "error":
                circle.setFill(Color.RED);
                break;
            default:
                circle.setFill(Color.GRAY);
                break;

        }
    }

    public Circle getCircle() {
        return circle;
    }
}
