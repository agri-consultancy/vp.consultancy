# Master Schedule Excel Import Format Guide

## Excel File Requirements

- **File Format**: `.xlsx` (Microsoft Excel 2007+)
- **File Size**: Maximum 5MB
- **Sheet Name**: Any name (first sheet will be used)
- **Header Row**: Required (Row 1)
- **Data Rows**: Starting from Row 2

---

## Excel Column Structure

| Column # | Field Name | Data Type | Required | Max Length | Notes |
|----------|-----------|-----------|----------|-----------|-------|
| A | day_number | Long | Yes | - | Must be unique per template, can have multiple rows with same day_number for multiple tasks |
| B | title | String | Yes | 255 | Phase/stage title (e.g., "Planting Day", "Early Growth") |
| C | description | String | No | 255 | Detailed description of the phase |
| D | fertilizer_name | String | Yes | 255 | Name of fertilizer/chemical to apply |
| E | quantity | String | Yes | 255 | Quantity with unit (e.g., "150kg", "25mm") |
| F | proportion | String | No | 50 | Proportion/percentage (e.g., "50%", "100%") |
| G | priority | Long | No | - | Priority number (1, 2, 3...) |
| H | task_type | String | No | 255 | Task classification (FERTILIZER, IRRIGATION, HERBICIDE, PESTICIDE) |
| I | task_description | String | No | 255 | Detailed instructions for this specific task |

---

## Example Excel Data

### Winter Wheat Schedule Example

```
| day_number | title | description | fertilizer_name | quantity | proportion | priority | task_type | task_description |
|------------|-------|-------------|-----------------|----------|-----------|----------|-----------|------------------|
| 1 | Planting Day | Initial Soil Prep & Seeding | Nitrogen Base | 150kg | 50% | 1 | FERTILIZER | Apply nitrogen base evenly across field |
| 1 | Planting Day | Initial Soil Prep & Seeding | | 25mm | | 1 | IRRIGATION | Apply sprinkler irrigation after planting |
| 1 | Planting Day | Initial Soil Prep & Seeding | Pre-emergent A | 2L | 100% | 2 | HERBICIDE | Apply pre-emergent herbicide spray |
| 14 | Early Growth | | Urea | 50kg | 100% | 1 | FERTILIZER | Broadcast urea fertilizer evenly |
| 14 | Early Growth | | | 15mm | | 1 | IRRIGATION | Apply drip irrigation |
| 30 | Tillering Stage | Peak tiller formation | NPK 20-20-20 | 100kg | 100% | 1 | FERTILIZER | Apply NPK fertilizer |
| 30 | Tillering Stage | Peak tiller formation | | 30mm | | 2 | IRRIGATION | Apply sprinkler irrigation |
| 30 | Tillering Stage | Peak tiller formation | General spray | 3L | 100% | 3 | PESTICIDE | Apply general pest control spray |
| 60 | Heading Stage | Ear emergence | Potassium Nitrate | 50kg | 80% | 1 | FERTILIZER | Apply potassium nitrate |
| 60 | Heading Stage | Ear emergence | | 50mm | | 1 | IRRIGATION | Apply flood irrigation |
| 60 | Heading Stage | Ear emergence | Preventative | 2L | 100% | 2 | FUNGICIDE | Apply preventative fungicide |
| 90 | Pre-Harvest | Grain filling stage | | | | | TASK | Stop irrigation immediately |
| 90 | Pre-Harvest | Grain filling stage | | | | | TASK | Equipment check and maintenance |
| 120 | Harvest Day | Ready for harvest | | | | | TASK | Combine harvesting ready |
| 120 | Harvest Day | Ready for harvest | | | | | TASK | Grain transport logistics |
```

---

## Key Points

### Multiple Tasks Per Day
- Multiple rows can have the same `day_number`
- Each row represents a separate task for that day
- Title and description should be the same for all rows with the same day_number
- Each task creates a separate record in the database

### Task Classification
- `FERTILIZER` - Chemical/organic fertilizer application
- `IRRIGATION` - Water management (sprinkler, drip, flood, etc.)
- `HERBICIDE` - Weed control
- `PESTICIDE` - Pest control
- `FUNGICIDE` - Disease control
- `TASK` - General task (equipment check, harvesting, etc.)

### Field Guidelines

#### day_number
- Must be a positive integer
- Can be any sequence (1, 14, 30, 60, 90, 120)
- Represents days from crop start
- Must be unique but can have multiple tasks

#### title
- **Do not leave blank** - This is the day/phase name
- Examples: "Planting Day", "Early Growth", "Heading Stage"

#### quantity
- **Do not leave blank** - This is required
- Include unit: "150kg", "50L", "25mm", "2 acres"
- Can be in any unit relevant to the task

#### fertilizer_name
- Can be left blank for non-fertilizer tasks (IRRIGATION, TASK types)
- Use actual product names if available

#### proportion
- Optional field
- Use when applying portion of a larger quantity
- Format: "50%", "25%", "100%"

---

## Common Errors & Solutions

| Error | Cause | Solution |
|-------|-------|----------|
| "day_number cannot be empty" | Column A is empty | Ensure all rows have day_number |
| "title cannot be empty" | Column B is empty | Add title for each day |
| "quantity cannot be empty" | Column E is empty | Add quantity (even if 0 or N/A) |
| "Invalid number format" | Non-numeric in day_number or priority | Use only numbers in these columns |
| "No valid schedule data found" | All rows are empty or only header exists | Add data rows starting from row 2 |
| "File is not .xlsx format" | Wrong file format | Export from Excel as .xlsx format |

---

## Import Process Flow

1. **Upload Excel file** via API endpoint
2. **Parser validates** each row (required fields, data types)
3. **Days are extracted** and grouped by day_number
4. **Tasks are associated** with their respective days
5. **Template created** with all days and tasks
6. **Transaction commits** on success
7. **Automatic rollback** if any error occurs

---

## Sample cURL Command

```bash
curl -X POST http://localhost:8080/api/consultant/schedule/import-excel \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -F "crop_variety_id=1" \
  -F "version=1" \
  -F "description=Winter Wheat standardized schedule" \
  -F "status=ACTIVE" \
  -F "file=@winter_wheat_schedule.xlsx" \
  -F "create_new_version=false"
```

---

## Sample Excel Template

You can create a basic Excel template with these headers:

```
day_number | title | description | fertilizer_name | quantity | proportion | priority | task_type | task_description
```

Then fill in your schedule data following the examples above.

---

## Notes

- Blank cells are treated as empty strings (null)
- Leading/trailing whitespace is trimmed automatically
- Day order in Excel doesn't affect import (sorted by day_number)
- Multiple tasks can be on the same day (see examples)
- After import, you can still add/edit individual days and tasks via APIs
