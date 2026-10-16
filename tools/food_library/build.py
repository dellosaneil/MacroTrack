"""
Builds composeApp/src/commonMain/composeResources/files/food_library.json from spec.txt.

The source is the USDA FoodData Central "SR Legacy" CSV download (public domain):
https://fdc.nal.usda.gov/fdc-datasets/FoodData_Central_sr_legacy_food_csv_2018-04.zip

Usage: python3 build.py <unzipped csv folder> spec.txt <output json>
"""
import csv, sys, json
base, spec, out = sys.argv[1], sys.argv[2], sys.argv[3]
desc_to_id = {}
with open(base + '/food.csv', newline='') as f:
    for r in csv.DictReader(f):
        desc_to_id.setdefault(r['description'], r['fdc_id'])
units = {}
with open(base + '/measure_unit.csv', newline='') as f:
    for r in csv.DictReader(f):
        units[r['id']] = r['name']
portions = {}
with open(base + '/food_portion.csv', newline='') as f:
    for r in csv.DictReader(f):
        unit = units.get(r['measure_unit_id'], '')
        unit = '' if unit == 'undetermined' else unit
        amount = r['amount']
        if amount.endswith('.0'):
            amount = amount[:-2]
        text = ' '.join(x for x in [amount, unit, r['modifier'] or r['portion_description']] if x)
        portions.setdefault(r['fdc_id'], []).append((text, float(r['gram_weight'])))
wanted = {'1008': 'kcal', '1003': 'protein', '1005': 'carbs', '1004': 'fat', '1079': 'fiber', '2000': 'sugar', '1093': 'sodium'}
nutrients = {}
with open(base + '/food_nutrient.csv', newline='') as f:
    for r in csv.DictReader(f):
        key = wanted.get(r['nutrient_id'])
        if key:
            nutrients.setdefault(r['fdc_id'], {})[key] = float(r['amount'])
foods, problems = [], []
for line in open(spec):
    line = line.strip()
    if not line or line.startswith('#'):
        continue
    parts = [p.strip() for p in line.split('|')]
    name, cat, desc, serving = parts[:4]
    popular = len(parts) > 4 and parts[4] == '*'
    fid = desc_to_id.get(desc)
    if not fid:
        problems.append(f'NO FOOD: {name}: {desc}')
        continue
    unit, amount, grams, label = 'g', None, None, None
    spec_portion = serving
    if serving.startswith('ml:'):
        ml, spec_portion = serving[3:].split('@', 1)
        unit, amount = 'ml', float(ml)
    if spec_portion.startswith('g:'):
        grams = float(spec_portion[2:])
    else:
        want = spec_portion[2:].lower()
        match = next(((t, g) for t, g in portions.get(fid, []) if want in t.lower()), None)
        if not match:
            problems.append(f'NO PORTION: {name}: {want} in {portions.get(fid, [])}')
            continue
        label, grams = match
    if amount is None:
        amount = round(grams)
    amount = int(amount) if float(amount).is_integer() else amount
    n = nutrients[fid]
    missing = [k for k in ('kcal', 'protein', 'carbs', 'fat') if k not in n]
    if missing:
        problems.append(f'MISSING {missing}: {name}')
    r = grams / 100
    foods.append({
        'name': name, 'category': cat, 'popular': popular, 'fdcId': int(fid),
        'serving': amount, 'unit': unit,
        'calories': round(n.get('kcal', 0) * r), 'protein': round(n.get('protein', 0) * r, 1),
        'carbs': round(n.get('carbs', 0) * r, 1), 'fat': round(n.get('fat', 0) * r, 1),
        'fiber': round(n['fiber'] * r, 1) if 'fiber' in n else None,
        'sugar': round(n['sugar'] * r, 1) if 'sugar' in n else None,
        'sodium': round(n['sodium'] * r) if 'sodium' in n else None,
    })
names = [f['name'] for f in foods]
dups = {x for x in names if names.count(x) > 1}
if dups:
    problems.append(f'DUPLICATE NAMES {dups}')
print('\n'.join(problems))
print(len(foods), 'foods')
json.dump(foods, open(out, 'w'), indent=None, separators=(',', ':'), ensure_ascii=False)
