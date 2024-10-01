from fastapi import FastAPI
from typing import List
from pydantic import BaseModel
from starlette.responses import JSONResponse

app=FastAPI( )

polls_list = []
votes_list = []

class Vote(BaseModel):
    vote_id: int
    poll_id: int
    answers: List[str]


class Poll(BaseModel):
    poll_id: int
    questions: List[str]


@app.get("/")
def root() :
    return {"message" : "Welcome to Polls"}


@app.get("/polls")
def get_polls():
    return polls_list

@app.post("/polls")
def create_poll(poll: Poll):
    if poll.poll_id in [p.poll_id for p in polls_list]:
        return JSONResponse(status_code=400, content={"message": "Poll already exists"})
    polls_list.append(poll)
    return poll


@app.get("/polls/{poll_id}")
def get_poll(poll_id: int):
    for poll in polls_list:
        if poll.poll_id == poll_id:
            return poll
    return JSONResponse(status_code=400, content={"message": "Poll not found"})

@app.put("/polls/{poll_id}")
def update_poll(poll_id: int, poll: Poll):
    for polly in polls_list:
        if polly.poll_id == poll_id:
            polls_list[polls_list.index(polly)] = poll
            return poll
    return JSONResponse(status_code=400, content={"message": "Poll not found"})

@app.delete("/polls/{poll_id}")
def delete_poll(poll_id: int):
    for poll in polls_list:
        if poll.poll_id == poll_id:
            del polls_list[polls_list.index(poll)]
            return JSONResponse(status_code=200, content={"message": "Poll deleted"})
    return JSONResponse(status_code=400, content={"message": "Poll not found"})


@app.get("/polls/{poll_id}/votes")
def get_votes(poll_id: int):
    if poll_id in [poll.poll_id for poll in polls_list]:
        return [vote for vote in votes_list if vote.poll_id == poll_id]
    return JSONResponse(status_code=400, content={"message": "Poll not found"})

@app.post("/polls/{poll_id}/votes")
def create_vote(poll_id: int, vote: Vote):
    if poll_id in [poll.poll_id for poll in polls_list]:
        if vote.vote_id not in [v.vote_id for v in votes_list]:
            votes_list.append(vote)
            return vote
        return JSONResponse(status_code=400, content={"message": "Vote already exists"})
    return JSONResponse(status_code=400, content={"message": "Poll not found"})


@app.get("/polls/{poll_id}/votes/{vote_id}")
def get_vote(poll_id: int, vote_id: int):
    if poll_id not in [poll.poll_id for poll in polls_list]:
        JSONResponse(status_code=400, content={"message": "Poll not found"})
    for vote in votes_list:
        if vote.poll_id == poll_id and vote.vote_id == vote_id:
            return vote
    return JSONResponse(status_code=400, content={"message": "Vote not found"})

@app.put("/polls/{poll_id}/votes/{vote_id}")
def update_vote(poll_id: int, vote_id: int, vote: Vote):
    pass

@app.delete("/polls/{poll_id}/votes/{vote_id}")
def delete_vote(poll_id: int, vote_id: int):
    pass