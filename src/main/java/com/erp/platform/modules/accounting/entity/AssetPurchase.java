package com.erp.platform.modules.accounting.entity;

import com.erp.platform.common.entity.TenantEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Asset Purchase transaction (per the Accounting module spec). On save it registers a fixed asset
 * and posts a balanced journal entry: the selected fixed-asset ledger is credited and the company
 * ledger is debited for the purchase total.
 */
@Entity
@Table(name = "asset_purchases",
       indexes = {@Index(name = "idx_ap_tenant", columnList = "tenant_id")})
@Getter
@Setter
public class AssetPurchase extends TenantEntity {

    @Column(name = "serial_number", length = 40)
    private String serialNumber;

    @Column(nullable = false, length = 200)
    private String name;

    @Column(name = "asset_group_code", length = 30)
    private String assetGroupCode;

    @Column(name = "asset_group_name", length = 100)
    private String assetGroupName;

    /** Fixed-asset ledger that is CREDITED. */
    @Column(name = "fixed_asset_ledger_id")
    private UUID fixedAssetLedgerId;

    @Column(name = "fixed_asset_ledger_code", length = 30)
    private String fixedAssetLedgerCode;

    @Column(name = "fixed_asset_ledger_name", length = 200)
    private String fixedAssetLedgerName;

    /** Company ledger that is DEBITED. */
    @Column(name = "company_ledger_id")
    private UUID companyLedgerId;

    @Column(name = "company_ledger_code", length = 30)
    private String companyLedgerCode;

    @Column(name = "company_ledger_name", length = 200)
    private String companyLedgerName;

    @Column(length = 100)
    private String location;

    @Column(length = 100)
    private String godown;

    @Column(name = "net_name", length = 100)
    private String net;

    @Column(name = "material_group", length = 100)
    private String materialGroup;

    @Column(name = "material_item", length = 100)
    private String materialItem;

    @Column(name = "item_count")
    private Integer itemCount = 1;

    @Column(name = "amount_per_item", precision = 18, scale = 2)
    private BigDecimal amountPerItem = BigDecimal.ZERO;

    @Column(name = "total_amount", precision = 18, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(length = 1000)
    private String narration;

    @Column(name = "entry_date")
    private LocalDate entryDate;

    @Column(name = "purchase_date")
    private LocalDate purchaseDate;

    @Column(name = "put_to_use_date")
    private LocalDate putToUseDate;

    /** CASH or BANK. */
    @Column(name = "mode_of_purchase", length = 10)
    private String modeOfPurchase;

    @Column(name = "depreciation_rate", precision = 8, scale = 4)
    private BigDecimal depreciationRate = BigDecimal.ZERO;

    @Column(name = "life_time_years")
    private Integer lifeTimeYears;

    /** Links back to what the save produced. */
    @Column(name = "fixed_asset_id")
    private UUID fixedAssetId;

    @Column(name = "journal_entry_id")
    private UUID journalEntryId;

    @Column(name = "journal_entry_number", length = 40)
    private String journalEntryNumber;

    /** Inventory lot created for this purchase (when a godown is selected). */
    @Column(name = "stock_lot_id")
    private UUID stockLotId;

    @Column(name = "stock_lot_no", length = 60)
    private String stockLotNo;
}
