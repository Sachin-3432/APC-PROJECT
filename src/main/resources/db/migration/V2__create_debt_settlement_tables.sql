-- Create debts table
CREATE TABLE debts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creditor_id BIGINT NOT NULL,
    debtor_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    settled_amount DECIMAL(19, 2) NOT NULL DEFAULT 0.00,
    expense_id BIGINT NOT NULL,
    group_id BIGINT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    settled_at TIMESTAMP NULL,
    
    FOREIGN KEY (creditor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (debtor_id) REFERENCES users(id) ON DELETE CASCADE,
    FOREIGN KEY (expense_id) REFERENCES expenses(id) ON DELETE CASCADE,
    FOREIGN KEY (group_id) REFERENCES groups(id) ON DELETE CASCADE,
    
    INDEX idx_creditor (creditor_id),
    INDEX idx_debtor (debtor_id),
    INDEX idx_expense (expense_id),
    INDEX idx_group (group_id),
    INDEX idx_status (status),
    INDEX idx_created_at (created_at)
);

-- Create settlements table
CREATE TABLE settlements (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    debt_id BIGINT NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    settled_by_id BIGINT NOT NULL,
    description VARCHAR(500),
    payment_method VARCHAR(50) NOT NULL DEFAULT 'CASH',
    settlement_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    FOREIGN KEY (debt_id) REFERENCES debts(id) ON DELETE CASCADE,
    FOREIGN KEY (settled_by_id) REFERENCES users(id) ON DELETE CASCADE,
    
    INDEX idx_debt (debt_id),
    INDEX idx_settled_by (settled_by_id),
    INDEX idx_settlement_date (settlement_date)
);
