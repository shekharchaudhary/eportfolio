from .animal_shelter import AnimalShelter
from bson.objectid import ObjectId

animals = AnimalShelter("aacuser", "shekhar1499")

#Valid document create
print(animals.create({
    'age_upon_outcome': "2 months",
    'animal_id': "test_123",
    'animal_type': "Dog",
    'breed': "Labrador Retriever Mix",
    'color': "Brown and White",
    'date_of_birth': "2023-01-15",
    'datetime': "2023-03-15 14:30:00",
    'monthyear': "2023-03-15T14:30:00",
    'name': "Buddy",
    'outcome_subtype': "Foster",
    'outcome_type': 'Transfer',
    'sex_upon_outcome': "Neutered Male",
    'location_lat': 34.052235,
    'location_long': -118.243683,
    'age_upon_outcome_in_weeks': 8.714285714285714
}))


#Invalid Document
print(animals.create({0:0}))

#Valid query
query = animals.read({"name": "Buddy"})
for animal in query:
    print(animal)
    
#invalid query throws exception
print(list(animals.read({0:0})))
