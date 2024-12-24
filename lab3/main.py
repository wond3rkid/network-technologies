import asyncio
import aiohttp
import json
import urllib.parse

LOCATIONS_URL = "https://graphhopper.com/api/1/geocode?q=%s&locale=en&limit=10&debug=true&key=317c1159-28b7-4634-bf8c-af7bac286214"
WEATHER_URL = "https://api.openweathermap.org/data/2.5/weather?lat={}&lon={}&appid=ccd24265aadfb5ea7b66356bb3647428"
PLACES_URL = "https://api.opentripmap.com/0.1/ru/places/radius?radius=5000&lon={}&lat={}&format=json&limit=100&apikey=5ae2e3f221c38a28845f05b6a2895c8d6ff5c2ab850f2c033e521f3a"
DESCRIPTION_URL = "https://api.opentripmap.com/0.1/ru/places/xid/{}?apikey=5ae2e3f221c38a28845f05b6a2895c8d6ff5c2ab850f2c033e521f3a"

location_map = {}


async def fetch(session, url):
    async with session.get(url, ssl=False) as response:
        return await response.text()


async def find_locations(session, substring):
    if not substring:
        return [""]
    encoded_substring = urllib.parse.quote(substring)
    url = LOCATIONS_URL % encoded_substring
    response = await fetch(session, url)
    data = json.loads(response)
    hits = data.get("hits", [])
    fill_locations(hits)
    return list(location_map.keys())


def fill_locations(hits):
    global location_map
    location_map.clear()
    for hit in hits:
        name = f"{hit['name']}, {hit['country']}"
        position = (hit['point']['lat'], hit['point']['lng'])
        location_map[name] = position


async def find_weather(session, key):
    lat, lon = location_map.get(key)
    url = WEATHER_URL.format(lat, lon)
    response = await fetch(session, url)
    data = json.loads(response)
    temp = data["main"]["temp"] - 273.15
    return f"{round(temp, 2)}°C"


async def find_places(session, key):
    lat, lon = location_map.get(key)
    url = PLACES_URL.format(lon, lat)
    response = await fetch(session, url)
    places = json.loads(response)
    return {place["name"]: place["xid"] for place in places}


async def find_place_description(session, xid):
    url = DESCRIPTION_URL.format(xid)
    response = await fetch(session, url)
    data = json.loads(response)
    if data.get('error', None) is not None:
        return None
    info = data.get("info", {})
    name = data.get("name")
    descr = info.get("descr")
    return f"{name}\n{descr}"


async def main():
    async with aiohttp.ClientSession() as session:
        query = input("Введите название места: ")
        locations = await find_locations(session, query)

        if not locations:
            print("Мест не найдено.")
            return

        print("Выберите локацию:")
        for i, location in enumerate(locations, start=1):
            print(f"{i}. {location}")

        try:
            selected_index = int(input("Введите номер выбранной локации: ")) - 1
            if selected_index < 0 or selected_index >= len(locations):
                print("Неверный номер.")
                return
        except ValueError:
            print("Введите корректный номер.")
            return

        selected_location = locations[selected_index]

        weather = await find_weather(session, selected_location)
        print(f"Погода: {weather}")

        places = await find_places(session, selected_location)
        if not places:
            print("Интересных мест не найдено.")
            return

        print("\nИнтересные места и их описания:")
        tasks = [find_place_description(session, xid) for xid in places.values()]
        descriptions = await asyncio.gather(*tasks)
        for description in descriptions:
            if description is not None:
                print(description)


asyncio.run(main())
