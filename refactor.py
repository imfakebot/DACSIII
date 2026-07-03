import os
import re
import shutil

base_dir = "/mnt/Data/Project/DACSII/Datsan/app/src/main/java/com/tanh/datsan"

moves = {
    # Auth
    "AuthViewModel.kt": "com.tanh.datsan.ui.auth",
    "AuthUiState.kt": "com.tanh.datsan.ui.auth",
    "AuthUiEvent.kt": "com.tanh.datsan.ui.auth",
    
    # Home/Main
    "HomeViewModel.kt": "com.tanh.datsan.ui.home.main",
    
    # Home/Detail
    "DetailViewModel.kt": "com.tanh.datsan.ui.home.detail",
    "DetailUiState.kt": "com.tanh.datsan.ui.home.detail",
    "BookingUiState.kt": "com.tanh.datsan.ui.home.detail",
    
    # Home/Booking
    "BookingSuccessViewModel.kt": "com.tanh.datsan.ui.home.booking",
    "BookingReceiptUiState.kt": "com.tanh.datsan.ui.home.booking",
    
    # Home/History
    "BookingHistoryViewModel.kt": "com.tanh.datsan.ui.home.history",
    "BookingHistoryUiState.kt": "com.tanh.datsan.ui.home.history",
    
    # Home/Review
    "ReviewViewModel.kt": "com.tanh.datsan.ui.home.review",
    "ReviewUiState.kt": "com.tanh.datsan.ui.home.review",
    
    # Home/Voucher
    "VoucherViewModel.kt": "com.tanh.datsan.ui.home.voucher",
    
    # Home/Notification
    "NotificationViewModel.kt": "com.tanh.datsan.ui.home.notification",
    
    # Profile
    "ProfileViewModel.kt": "com.tanh.datsan.ui.profile",
    "ProfileUiState.kt": "com.tanh.datsan.ui.profile",
    "UserViewModel.kt": "com.tanh.datsan.ui.profile",
    
    # Admin/Booking
    "AdminBookingViewModel.kt": "com.tanh.datsan.ui.admin.booking",
    "AdminCreateBookingViewModel.kt": "com.tanh.datsan.ui.admin.booking",
    "QAdminCreateBookingUiState.kt": "com.tanh.datsan.ui.admin.booking",
    
    # Admin/Branch
    "BranchViewModel.kt": "com.tanh.datsan.ui.admin.branch",
    "BranchState.kt": "com.tanh.datsan.ui.admin.branch",
    
    # Admin/Category
    "AdminFieldTypeViewModel.kt": "com.tanh.datsan.ui.admin.category",
    "AdminFieldTypeState.kt": "com.tanh.datsan.ui.admin.category",
    "AdminUtilityViewModel.kt": "com.tanh.datsan.ui.admin.category",
    "AdminUtilityState.kt": "com.tanh.datsan.ui.admin.category",
    
    # Admin/Feedback
    "AdminFeedbackViewModel.kt": "com.tanh.datsan.ui.admin.feedback",
    
    # Admin/Field
    "AdminFieldViewModel.kt": "com.tanh.datsan.ui.admin.field",
    "AdminUiState.kt": "com.tanh.datsan.ui.admin.field",
    
    # Admin/Pricing
    "AdminTimeSlotViewModel.kt": "com.tanh.datsan.ui.admin.pricing",
    "AdminTimeSlotUiState.kt": "com.tanh.datsan.ui.admin.pricing",
    
    # Admin/Review
    "AdminReviewViewModel.kt": "com.tanh.datsan.ui.admin.review",
    "AdminReviewUiState.kt": "com.tanh.datsan.ui.admin.review",
    
    # Admin/User
    "AdminUserViewModel.kt": "com.tanh.datsan.ui.admin.user",
    "AdminUserState.kt": "com.tanh.datsan.ui.admin.user",
    
    # Admin/Voucher
    "AdminVoucherViewModel.kt": "com.tanh.datsan.ui.admin.voucher",
    "AdminVoucherUiState.kt": "com.tanh.datsan.ui.admin.voucher",
    
    # Admin Stats
    "StatisticsViewModel.kt": "com.tanh.datsan.ui.admin",
    "StatisticsUiState.kt": "com.tanh.datsan.ui.admin",
    
    # Feedback
    "FeedbackListViewModel.kt": "com.tanh.datsan.ui.feedback",
    "FeedbackListUiState.kt": "com.tanh.datsan.ui.feedback",
    
    # Staff
    "QrScannerViewModel.kt": "com.tanh.datsan.ui.staff",
    "CheckInUiState.kt": "com.tanh.datsan.ui.staff",
    
    # Global state / Others
    "UIState.kt": "com.tanh.datsan.core", 
    "ActionState.kt": "com.tanh.datsan.core",
    "MainViewModel.kt": "com.tanh.datsan.ui.home.main"
}

source_dirs = [
    os.path.join(base_dir, "viewmodel"),
    os.path.join(base_dir, "ui", "state")
]

import_replacements = {}

# Process each file to move
for src_dir in source_dirs:
    if not os.path.exists(src_dir):
        continue
    for filename in os.listdir(src_dir):
        if filename.endswith(".kt") and filename in moves:
            old_path = os.path.join(src_dir, filename)
            new_pkg = moves[filename]
            
            # Determine old package to make correct import replacement
            with open(old_path, 'r', encoding='utf-8') as f:
                content = f.read()
            old_pkg_match = re.search(r'^package\s+([a-zA-Z0-9_.]+)', content, flags=re.MULTILINE)
            if not old_pkg_match:
                continue
            old_pkg = old_pkg_match.group(1)
            
            # Read classes inside the file to map old import to new import
            # For simplicity, we just replace import com.tanh.datsan.viewmodel.ClassName with new import
            class_name = filename.replace(".kt", "")
            import_replacements[f"import {old_pkg}.{class_name}"] = f"import {new_pkg}.{class_name}"
            
            # In case there are multiple classes in the file, like UserStats in ProfileUiState.kt,
            # or UiEvent in UIState.kt. We can do regex match for all top level declarations:
            declarations = re.findall(r'^(?:data\s+)?(?:sealed\s+)?(?:class|interface|object)\s+([A-Z][a-zA-Z0-9_]+)', content, flags=re.MULTILINE)
            for decl in declarations:
                import_replacements[f"import {old_pkg}.{decl}"] = f"import {new_pkg}.{decl}"
            
            # Update package inside the file
            content = re.sub(r'^package\s+[a-zA-Z0-9_.]+', f"package {new_pkg}", content, flags=re.MULTILINE)
            
            # Create target directory
            target_dir = os.path.join("/mnt/Data/Project/DACSII/Datsan/app/src/main/java", new_pkg.replace(".", "/"))
            os.makedirs(target_dir, exist_ok=True)
            new_path = os.path.join(target_dir, filename)
            
            with open(new_path, 'w', encoding='utf-8') as f:
                f.write(content)
                
            os.remove(old_path)
            print(f"Moved {filename} to {target_dir}")

# Now replace imports in all kt files
print(f"Applying import replacements: {len(import_replacements)}")
for root, _, files in os.walk(base_dir):
    for filename in files:
        if filename.endswith(".kt"):
            filepath = os.path.join(root, filename)
            with open(filepath, 'r', encoding='utf-8') as f:
                content = f.read()
            
            original = content
            for old_imp, new_imp in import_replacements.items():
                content = content.replace(old_imp, new_imp)
            
            if content != original:
                with open(filepath, 'w', encoding='utf-8') as f:
                    f.write(content)
                print(f"Updated imports in {filepath}")

# Remove empty dirs
for src_dir in source_dirs:
    if os.path.exists(src_dir) and not os.listdir(src_dir):
        os.rmdir(src_dir)
        print(f"Removed empty dir {src_dir}")
