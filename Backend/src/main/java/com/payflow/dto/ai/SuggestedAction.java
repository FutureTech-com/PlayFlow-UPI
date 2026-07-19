package com.payflow.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SuggestedAction {
    private String label; // e.g. "Send money"
    private String route; // e.g. "/send"
}
