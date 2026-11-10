import csv
import json
import re

students = []
with open("public/assets/aistudio/database_siswa_2026_a6d5a485.csv", "r", encoding="utf-8") as f:
    reader = csv.reader(f)
    rows = list(reader)

# header is at row 2 (0-indexed)
header = rows[2]
print("Header:", header)

data_rows = rows[3:]
print(f"Data rows count: {len(data_rows)}")

for idx, row in enumerate(data_rows):
    if not row or not any(row):
        continue
    # Clean up zero-width spaces or odd characters
    cleaned_row = [re.sub(r"[\u200b-\u200f\ufeff]", "", c).strip() for c in row]
    while len(cleaned_row) < 13:
        cleaned_row.append("")
    
    no, name, kelas, jenjang, alamat, jk, nik, no_kk, tempat_lahir, tgl_lahir, nama_ibu, nisn, kamar = cleaned_row[:13]
    
    # Clean gender: "L" or "P"
    gender = "L" if "L" in jk.upper() else "P"
    
    # Calculate age from tgl_lahir (DD-MM-YYYY)
    age = 12
    if tgl_lahir:
        parts = tgl_lahir.split("-")
        if len(parts) == 3 and parts[2].isdigit():
            birth_year = int(parts[2])
            age = max(6, min(22, 2026 - birth_year))
    elif "SD" in jenjang or "SD" in kelas:
        age = 9
    elif "SMP" in jenjang or "VII" in kelas:
        age = 13
    elif "SMA" in jenjang or "X" in kelas or "XI" in kelas:
        age = 16
    
    # Dormitory: Asrama Putra / Putri + room if available
    if kamar:
        dorm = f"Asrama {'Putra' if gender == 'L' else 'Putri'} (Kamar {kamar})"
    else:
        dorm = f"Asrama {'Putra' if gender == 'L' else 'Putri'}"
        
    students.append({
        "id": idx + 1,
        "name": name,
        "nis": nisn if nisn else f"SRT1-{idx+1:04d}",
        "gender": gender,
        "age": age,
        "dormitory": dorm,
        "classGrade": f"Kelas {kelas}",
        "allergies": "Tidak ada",
        "bloodType": "-",
        "emergencyContact": f"Ibu: {nama_ibu}" if nama_ibu else "Pengasuh Asrama",
        "specialNotes": f"Alamat: {alamat}" if alamat else "",
        "nik": nik,
        "noKk": no_kk,
        "birthPlace": tempat_lahir,
        "birthDate": tgl_lahir,
        "motherName": nama_ibu,
        "address": alamat,
        "roomNumber": kamar,
        "jenjang": jenjang
    })

print(f"Successfully processed {len(students)} students!")
print("Sample student 1:", json.dumps(students[0], indent=2))
print("Sample student 342:", json.dumps(students[-1], indent=2))

with open("/tmp/students_seed.json", "w", encoding="utf-8") as out:
    json.dump(students, out, ensure_ascii=False, indent=2)
