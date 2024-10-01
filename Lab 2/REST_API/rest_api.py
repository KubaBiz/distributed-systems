from fastapi import FastAPI, Form, Request
from fastapi.responses import JSONResponse
from fastapi.templating import Jinja2Templates
from dotenv import load_dotenv

import httpx, os

load_dotenv()

app = FastAPI()

templates = Jinja2Templates(directory="templates")
quotes_url = "https://zenquotes.io/api/random"
currencies_url = "https://api.fxratesapi.com/currencies"
currencies_url_latest = "https://api.fxratesapi.com/latest?"

global currencies
currencies = []

hidden_key = os.getenv("HIDDEN_KEY")
fxrates_api_key = os.getenv("FXRATES_API_KEY")

@app.get("/")
async def get_root(request: Request):
    return templates.TemplateResponse("index.html", {"request": request})

async def get_quote():
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(quotes_url)
        except Exception as e:
            return JSONResponse(status_code=503, content=str(e))
        if response.status_code == 200:
            data = response.json()
            quote = data[0]["q"] + " ~ " + data[0]["a"]
            return quote
        return JSONResponse(status_code=503, content="Failed to fetch random quote")

async def get_currencies():
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(currencies_url)
        except Exception as e:
            return JSONResponse(status_code=503, content=str(e))
        if response.status_code == 200:
            temp = response.json()
            currency_list = []
            for name, dictionary in temp.items():
                currency_list.append([name, dictionary["name"]])
            global currencies
            currencies = currency_list
            return currency_list
        return JSONResponse(status_code=503, content="Failed to fetch currencies")

@app.get("/currency/")
async def currency_panel(request: Request):
    global currencies
    if len(currencies) == 0:
        temp =  await get_currencies()
        if type(temp) is JSONResponse:
            return temp
        return templates.TemplateResponse("currencies.html", {"request": request, "currencies": currencies})
    return templates.TemplateResponse("currencies.html", {"request": request,"currencies": currencies})

async def exchange_money(currency_1, currency_2, amount):
    first_string = "api_key=" + fxrates_api_key
    second_string = "&base=" + currency_1
    third_string = "&currencies=" + currency_2
    final_url = currencies_url_latest + first_string + second_string + third_string
    async with httpx.AsyncClient() as client:
        try:
            response = await client.get(final_url)
        except Exception as e:
            return JSONResponse(status_code=503, content=str(e))
        if response.status_code == 200:
            temp = response.json()
            currency_2_amount = temp['rates'][currency_2] * amount
            
            return round(currency_2_amount, 3), round(temp['rates'][currency_2], 3)
        return JSONResponse(status_code=503, content=f"Failed to fetch latest currencies for {currency_1} and {currency_2}")

@app.post("/exchange/")
async def exchange_currency(request: Request, currency_1: str = Form('currency_1'), currency_2: str = Form('currency_2'), amount: float = Form('amount'), quote: bool = Form('quote'), key: str = Form('key')):
    if key != hidden_key:
        return JSONResponse(status_code=401, content="Not authorized API Key")
    if amount < 0:
        return JSONResponse(status_code=418, content="Donkey! You cannot set amount of currency to negative number!")
    global currencies
    if len(currencies) == 0:
        temp =  await get_currencies()
        if type(temp) is JSONResponse:
            return temp
    if not any(currency_1 in sublist for sublist in currencies):
        return JSONResponse(status_code=400, content=f"There is no such currency like {currency_1}")
    if not any(currency_2 in sublist for sublist in currencies):
        return JSONResponse(status_code=400, content=f"There is no such currency like {currency_2}")
    data = await exchange_money(currency_1, currency_2, amount)
    if type(data) is JSONResponse: 
        return data
    if quote == True:
        quote_string = await get_quote()
        if type(quote_string) is JSONResponse:
            return quote_string
        return templates.TemplateResponse("response.html", {"request": request, "quote": quote_string, "currency_1": currency_1, "currency_2": currency_2, "amount": amount, "data": data})
    return templates.TemplateResponse("response.html", {"request": request, "currency_1": currency_1, "currency_2": currency_2, "amount": amount, "data": data})

@app.post("/exchange_raw/")
async def exchange_currency(request: Request, currency_1: str = Form('currency_1'), currency_2: str = Form('currency_2'), amount: float = Form('amount'), quote: bool = Form('quote'), key: str = Form('key')):
    if key != hidden_key:
        return JSONResponse(status_code=401, content="Not authorized API Key")
    if amount < 0:
        return JSONResponse(status_code=418, content="Donkey! You cannot set amount of currency to negative number!")
    global currencies
    if len(currencies) == 0:
        temp =  await get_currencies()
        if type(temp) is JSONResponse:
            return temp
    if not any(currency_1 in sublist for sublist in currencies):
        return JSONResponse(status_code=400, content=f"There is no such currency like {currency_1}")
    if not any(currency_2 in sublist for sublist in currencies):
        return JSONResponse(status_code=400, content=f"There is no such currency like {currency_2}")
    data = await exchange_money(currency_1, currency_2, amount)
    if type(data) is JSONResponse: 
        return data
    if quote == True:
        quote_string = await get_quote()
        if type(quote_string) is JSONResponse:
            return quote_string
        return JSONResponse(status_code=200, content={"base": currency_1, "currency": currency_2, "exchange_rate": data[1], "amount_base": amount, "amount_currency": data[0], "quote": quote_string})
    return JSONResponse(status_code=200, content={"base": currency_1, "currency": currency_2, "exchange_rate": data[1], "amount_base": amount, "amount_currency": data[0]})
        