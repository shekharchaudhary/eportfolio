from pymongo import MongoClient
from bson.objectid import ObjectId

class AnimalShelter(object):
    """ CRUD operations for Animal collection in MongoDB """

    def __init__(self):
        # Initializing the MongoClient. This helps to 
        # access the MongoDB databases and collections.
        # This is hard-wired to use the aac database, the 
        # animals collection, and the aac user.
        # Definitions of the connection string variables are
        # unique to the individual Apporto environment.
        #
        # You must edit the connection variables below to reflect
        # your own instance of MongoDB!
        #
        # Connection Variables
        #
        USER = 'aacuser'
        PASS = 'shekhar1499'
        HOST = 'nv-desktop-services.apporto.com'
        PORT =33292
        DB = 'AAC'
        COL = 'animals'
        #
        # Initialize Connection
        #
        self.client = MongoClient('mongodb://%s:%s@%s:%d' % (USER,PASS,HOST,PORT))
        self.database = self.client['%s' % (DB)]
        self.collection = self.database['%s' % (COL)]

# Complete this create method to implement the C in CRUD.
    def create(self, data):
        if data is not None:
            # validating data is a dictionary
            insert_success = self.database.animals.insert_one(data)  # data should be dictionary 
            
            if insert_success != 0:
                return False
            else:
                return True
        else:
            raise Exception("Nothing to save, because data parameter is empty")
            
# Complete this create method to implement the R in CRUD

    def read(self, criteria):
        if criteria is not None:
            data = self.database.animals.find(criteria)
            for document in data:
                print(document)
        else:
            raise Exception("Nothing to read, because data parameter is empty") 
        return data

# Complete this create method to implement the U in CRUD  
    def update(self,searchData,updateData):
        if updateData is not None:
            if searchData:
                result = self.database.animals.update_one(searchData, updateData)
        else:
            raise Exception("Nothing to update, because data parameter is empty")
        # return the raw result of the update_one method
        return result.raw_result
            
# Complete this create method to implement the D in CRUD
    def delete(self, deleteData):
        if deleteData is not None:
            result = self.database.animals.delete_one(deleteData)
        else:
            raise Exception("Nothing to delete, because data parameter is empty")
        return result.raw_result
                
